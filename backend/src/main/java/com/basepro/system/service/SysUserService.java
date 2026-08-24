package com.basepro.system.service;

import cn.idev.excel.FastExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.security.LoginUserService;
import com.basepro.security.SecurityUtils;
import com.basepro.system.dto.UserImportResult;
import com.basepro.system.dto.UserQuery;
import com.basepro.system.entity.SysUser;
import com.basepro.system.entity.SysUserPost;
import com.basepro.system.entity.SysUserRole;
import com.basepro.system.mapper.SysUserMapper;
import com.basepro.system.mapper.SysUserPostMapper;
import com.basepro.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户。密码一律经 {@link PasswordEncoder} 加密后落库，岗位、角色关联在同一事务里同步维护。
 */
@Service
@RequiredArgsConstructor
public class SysUserService {

    /**
     * 状态：正常
     */
    private static final int STATUS_ENABLE = 0;

    /**
     * 导入新用户时的初始密码，导入后应提醒用户自行修改
     */
    private static final String IMPORT_DEFAULT_PASSWORD = "123456";

    private final SysUserMapper userMapper;
    private final SysUserPostMapper userPostMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDeptService deptService;
    private final PasswordEncoder passwordEncoder;
    private final LoginUserService loginUserService;

    public PageResult<SysUser> page(UserQuery query) {
        // 按部门筛选时连子部门一起查
        List<Long> deptIds = deptService.selfAndChildIds(query.getDeptId());
        Page<SysUser> page = userMapper.selectPage(query.toPage(), Wrappers.<SysUser>lambdaQuery()
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getMobile()), SysUser::getMobile, query.getMobile())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .in(!deptIds.isEmpty(), SysUser::getDeptId, deptIds)
                .ge(query.beginTime() != null, SysUser::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysUser::getCreateTime, query.endTime())
                .orderByDesc(SysUser::getId));
        fillDeptName(page.getRecords());
        return PageResult.of(page);
    }

    public List<SysUser> simpleList() {
        return userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .select(SysUser::getId, SysUser::getNickname)
                .eq(SysUser::getStatus, STATUS_ENABLE)
                .orderByAsc(SysUser::getId));
    }

    /**
     * 用户详情，附带岗位、角色编号，供编辑表单回显
     */
    public SysUser get(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setPostIds(postIds(id));
        user.setRoleIds(roleIds(id));
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(SysUser user) {
        user.setId(null);
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BizException("用户密码不能为空");
        }
        validateUnique(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(STATUS_ENABLE);
        }
        userMapper.insert(user);
        saveUserPost(user.getId(), user.getPostIds());
        saveUserRole(user.getId(), user.getRoleIds());
        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SysUser user) {
        validateExists(user.getId());
        validateUnique(user);
        // 密码只能通过重置密码、个人中心修改，这里忽略
        user.setPassword(null);
        userMapper.updateById(user);
        saveUserPost(user.getId(), user.getPostIds());
        saveUserRole(user.getId(), user.getRoleIds());
        // 部门、角色的变化直接影响鉴权结果，清掉缓存让其立即生效
        loginUserService.evict(user.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        if (ids.contains(SecurityUtils.getUserId())) {
            throw new BizException("不能删除自己");
        }
        for (Long id : ids) {
            validateExists(id);
        }
        userMapper.deleteByIds(ids);
        // 同步清理岗位、角色关联，避免残留脏数据
        userPostMapper.delete(Wrappers.<SysUserPost>lambdaQuery().in(SysUserPost::getUserId, ids));
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getUserId, ids));
        ids.forEach(loginUserService::evict);
    }

    /**
     * 管理员重置密码
     */
    public void updatePassword(Long id, String password) {
        validateExists(id);
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(update);
        loginUserService.evict(id);
    }

    public void updateStatus(Long id, Integer status) {
        validateExists(id);
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(status);
        userMapper.updateById(update);
        loginUserService.evict(id);
    }

    /**
     * 导入用户：逐行处理，单行失败只记录原因，不影响其它行。
     *
     * @param updateSupport 账号已存在时是否更新，否则记为失败
     */
    public UserImportResult importUsers(MultipartFile file, boolean updateSupport) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException("请上传文件");
        }
        List<Map<Integer, String>> rows;
        try (InputStream inputStream = file.getInputStream()) {
            // 无实体映射，按列下标读取，第一行为表头
            rows = FastExcel.read(inputStream).sheet().headRowNumber(1).doReadSync();
        }
        List<String> createUsernames = new ArrayList<>();
        List<String> updateUsernames = new ArrayList<>();
        Map<String, String> failureUsernames = new LinkedHashMap<>();
        for (Map<Integer, String> row : rows) {
            String username = cell(row, 0);
            if (!StringUtils.hasText(username)) {
                continue;
            }
            try {
                importRow(row, username, updateSupport, createUsernames, updateUsernames);
            } catch (Exception ex) {
                failureUsernames.put(username, ex.getMessage());
            }
        }
        return new UserImportResult(createUsernames, updateUsernames, failureUsernames);
    }

    private void importRow(Map<Integer, String> row, String username, boolean updateSupport,
                           List<String> createUsernames, List<String> updateUsernames) {
        SysUser existing = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username), false);
        if (existing != null && !updateSupport) {
            throw new BizException("用户账号已存在");
        }
        SysUser user = existing == null ? new SysUser() : existing;
        String nickname = cell(row, 1);
        user.setUsername(username);
        user.setNickname(StringUtils.hasText(nickname) ? nickname : username);
        user.setDeptId(parseLong(cell(row, 2)));
        user.setEmail(cell(row, 3));
        user.setMobile(cell(row, 4));
        user.setRemark(cell(row, 5));
        validateUnique(user);
        if (existing == null) {
            user.setStatus(STATUS_ENABLE);
            user.setPassword(passwordEncoder.encode(IMPORT_DEFAULT_PASSWORD));
            userMapper.insert(user);
            createUsernames.add(username);
            return;
        }
        // 导入不改密码
        user.setPassword(null);
        userMapper.updateById(user);
        updateUsernames.add(username);
        loginUserService.evict(user.getId());
    }

    private void validateExists(Long id) {
        if (id == null || userMapper.selectById(id) == null) {
            throw new BizException("用户不存在");
        }
    }

    private String cell(Map<Integer, String> row, int index) {
        String value = row.get(index);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BizException("部门编号不是合法数字：" + value);
        }
    }

    private void fillDeptName(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        Map<Long, String> deptNames = deptService.nameMap();
        users.forEach(user -> {
            if (user.getDeptId() != null) {
                user.setDeptName(deptNames.get(user.getDeptId()));
            }
        });
    }

    private List<Long> postIds(Long userId) {
        return userPostMapper.selectList(Wrappers.<SysUserPost>lambdaQuery()
                        .select(SysUserPost::getPostId)
                        .eq(SysUserPost::getUserId, userId))
                .stream()
                .map(SysUserPost::getPostId)
                .toList();
    }

    private List<Long> roleIds(Long userId) {
        return userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .select(SysUserRole::getRoleId)
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    /**
     * 关联表按“先删旧、再插新”维护。传 null 表示本次不改动关联关系。
     */
    private void saveUserPost(Long userId, List<Long> postIds) {
        if (postIds == null) {
            return;
        }
        userPostMapper.delete(Wrappers.<SysUserPost>lambdaQuery().eq(SysUserPost::getUserId, userId));
        postIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(postId -> userPostMapper.insert(SysUserPost.of(userId, postId)));
    }

    private void saveUserRole(Long userId, List<Long> roleIds) {
        if (roleIds == null) {
            return;
        }
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(roleId -> userRoleMapper.insert(SysUserRole.of(userId, roleId)));
    }

    /**
     * 账号、手机号、邮箱在租户内唯一（逻辑删除的记录不参与比较）
     */
    private void validateUnique(SysUser user) {
        if (StringUtils.hasText(user.getUsername())) {
            Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getUsername, user.getUsername())
                    .ne(user.getId() != null, SysUser::getId, user.getId()));
            if (count > 0) {
                throw new BizException("用户账号已存在");
            }
        }
        if (StringUtils.hasText(user.getMobile())) {
            Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getMobile, user.getMobile())
                    .ne(user.getId() != null, SysUser::getId, user.getId()));
            if (count > 0) {
                throw new BizException("手机号已存在");
            }
        }
        if (StringUtils.hasText(user.getEmail())) {
            Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getEmail, user.getEmail())
                    .ne(user.getId() != null, SysUser::getId, user.getId()));
            if (count > 0) {
                throw new BizException("邮箱已存在");
            }
        }
    }

}
