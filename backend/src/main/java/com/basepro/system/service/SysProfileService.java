package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.common.BizException;
import com.basepro.security.LoginUserService;
import com.basepro.security.SecurityUtils;
import com.basepro.system.dto.ProfileUpdateReq;
import com.basepro.system.dto.ProfileVO;
import com.basepro.system.dto.UpdatePasswordReq;
import com.basepro.system.entity.SysDept;
import com.basepro.system.entity.SysPost;
import com.basepro.system.entity.SysRole;
import com.basepro.system.entity.SysUser;
import com.basepro.system.entity.SysUserPost;
import com.basepro.system.entity.SysUserRole;
import com.basepro.system.mapper.SysDeptMapper;
import com.basepro.system.mapper.SysPostMapper;
import com.basepro.system.mapper.SysRoleMapper;
import com.basepro.system.mapper.SysUserMapper;
import com.basepro.system.mapper.SysUserPostMapper;
import com.basepro.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 个人中心。只操作当前登录用户，不接受外部传入的用户编号。
 */
@Service
@RequiredArgsConstructor
public class SysProfileService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysPostMapper postMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginUserService loginUserService;

    public ProfileVO get() {
        SysUser user = currentUser();
        return new ProfileVO(user.getId(), user.getUsername(), user.getNickname(),
                dept(user.getDeptId()), roles(user.getId()), posts(user.getId()),
                user.getEmail(), user.getMobile(), user.getSex(), user.getAvatar(),
                user.getStatus(), user.getRemark(), user.getLoginIp(), user.getLoginDate(),
                user.getCreateTime());
    }

    /**
     * 只允许修改昵称、邮箱、手机号、性别、头像；为空的字段保持原值
     */
    public void update(ProfileUpdateReq request) {
        Long userId = SecurityUtils.getUserId();
        validateUnique(userId, request.mobile(), request.email());
        SysUser update = new SysUser();
        update.setId(userId);
        if (StringUtils.hasText(request.nickname())) {
            update.setNickname(request.nickname());
        }
        if (StringUtils.hasText(request.email())) {
            update.setEmail(request.email());
        }
        if (StringUtils.hasText(request.mobile())) {
            update.setMobile(request.mobile());
        }
        if (request.sex() != null) {
            update.setSex(request.sex());
        }
        if (StringUtils.hasText(request.avatar())) {
            update.setAvatar(request.avatar());
        }
        userMapper.updateById(update);
        // 昵称等信息随登录用户缓存，改完立即失效
        loginUserService.evict(userId);
    }

    public void updatePassword(UpdatePasswordReq request) {
        SysUser user = currentUser();
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(update);
        loginUserService.evict(user.getId());
    }

    private SysUser currentUser() {
        SysUser user = userMapper.selectById(SecurityUtils.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    private ProfileVO.Item dept(Long deptId) {
        if (deptId == null) {
            return null;
        }
        SysDept dept = deptMapper.selectById(deptId);
        return dept == null ? null : new ProfileVO.Item(dept.getId(), dept.getName());
    }

    private List<ProfileVO.Item> roles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .select(SysUserRole::getRoleId)
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                        .select(SysRole::getId, SysRole::getName)
                        .in(SysRole::getId, roleIds))
                .stream()
                .map(role -> new ProfileVO.Item(role.getId(), role.getName()))
                .toList();
    }

    private List<ProfileVO.Item> posts(Long userId) {
        List<Long> postIds = userPostMapper.selectList(Wrappers.<SysUserPost>lambdaQuery()
                        .select(SysUserPost::getPostId)
                        .eq(SysUserPost::getUserId, userId))
                .stream()
                .map(SysUserPost::getPostId)
                .toList();
        if (postIds.isEmpty()) {
            return List.of();
        }
        return postMapper.selectList(Wrappers.<SysPost>lambdaQuery()
                        .select(SysPost::getId, SysPost::getName)
                        .in(SysPost::getId, postIds))
                .stream()
                .map(post -> new ProfileVO.Item(post.getId(), post.getName()))
                .toList();
    }

    /**
     * 手机号、邮箱不能与其它用户重复
     */
    private void validateUnique(Long userId, String mobile, String email) {
        if (StringUtils.hasText(mobile)) {
            Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getMobile, mobile)
                    .ne(SysUser::getId, userId));
            if (count > 0) {
                throw new BizException("手机号已存在");
            }
        }
        if (StringUtils.hasText(email)) {
            Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getEmail, email)
                    .ne(SysUser::getId, userId));
            if (count > 0) {
                throw new BizException("邮箱已存在");
            }
        }
    }

}
