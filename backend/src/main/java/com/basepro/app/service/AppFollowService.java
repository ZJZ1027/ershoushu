package com.basepro.app.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.app.dto.UserFollowVO;
import com.basepro.business.BookConstants;
import com.basepro.business.entity.BuUserFollow;
import com.basepro.business.mapper.BuUserFollowMapper;
import com.basepro.common.BizException;
import com.basepro.common.PageQuery;
import com.basepro.common.PageResult;
import com.basepro.security.SecurityUtils;
import com.basepro.system.entity.SysRole;
import com.basepro.system.entity.SysUser;
import com.basepro.system.entity.SysUserRole;
import com.basepro.system.mapper.SysRoleMapper;
import com.basepro.system.mapper.SysUserMapper;
import com.basepro.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppFollowService {

    private final BuUserFollowMapper followMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFollow(Long followeeId) {
        Long followerId = SecurityUtils.getUserId();
        if (Objects.equals(followerId, followeeId)) {
            throw new BizException("不能关注自己");
        }
        assertAppUser(followeeId);
        BuUserFollow exist = followMapper.selectOne(Wrappers.<BuUserFollow>lambdaQuery()
                .eq(BuUserFollow::getFollowerId, followerId)
                .eq(BuUserFollow::getFolloweeId, followeeId), false);
        if (exist != null) {
            followMapper.physicalDeleteById(exist.getId());
            return false;
        }
        // 清理逻辑删除残留，避免 uk_user_follow 唯一索引冲突
        followMapper.physicalDeleteByPair(followerId, followeeId);
        BuUserFollow follow = new BuUserFollow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followMapper.insert(follow);
        return true;
    }

    public PageResult<UserFollowVO> followers(PageQuery query, Long userId) {
        assertAppUser(userId);
        Page<BuUserFollow> page = followMapper.selectPage(query.toPage(), Wrappers.<BuUserFollow>lambdaQuery()
                .eq(BuUserFollow::getFolloweeId, userId)
                .orderByDesc(BuUserFollow::getId));
        List<Long> userIds = page.getRecords().stream().map(BuUserFollow::getFollowerId).toList();
        return new PageResult<>(toVoList(userIds), page.getTotal());
    }

    public PageResult<UserFollowVO> following(PageQuery query, Long userId) {
        assertAppUser(userId);
        Page<BuUserFollow> page = followMapper.selectPage(query.toPage(), Wrappers.<BuUserFollow>lambdaQuery()
                .eq(BuUserFollow::getFollowerId, userId)
                .orderByDesc(BuUserFollow::getId));
        List<Long> userIds = page.getRecords().stream().map(BuUserFollow::getFolloweeId).toList();
        return new PageResult<>(toVoList(userIds), page.getTotal());
    }

    public long countFollowers(Long userId) {
        if (userId == null) {
            return 0;
        }
        Long count = followMapper.selectCount(Wrappers.<BuUserFollow>lambdaQuery()
                .eq(BuUserFollow::getFolloweeId, userId));
        return count == null ? 0 : count;
    }

    public long countFollowing(Long userId) {
        if (userId == null) {
            return 0;
        }
        Long count = followMapper.selectCount(Wrappers.<BuUserFollow>lambdaQuery()
                .eq(BuUserFollow::getFollowerId, userId));
        return count == null ? 0 : count;
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            return false;
        }
        Long count = followMapper.selectCount(Wrappers.<BuUserFollow>lambdaQuery()
                .eq(BuUserFollow::getFollowerId, followerId)
                .eq(BuUserFollow::getFolloweeId, followeeId));
        return count != null && count > 0;
    }

    private List<UserFollowVO> toVoList(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        Map<Long, SysUser> users = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        Long loginId = SecurityUtils.getLoginUserOrNull() == null ? null : SecurityUtils.getUserId();
        Set<Long> followingIds = Set.of();
        if (loginId != null) {
            followingIds = followMapper.selectList(Wrappers.<BuUserFollow>lambdaQuery()
                            .eq(BuUserFollow::getFollowerId, loginId)
                            .in(BuUserFollow::getFolloweeId, userIds))
                    .stream()
                    .map(BuUserFollow::getFolloweeId)
                    .collect(Collectors.toSet());
        }
        final Set<Long> followed = followingIds;
        return userIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .map(user -> new UserFollowVO(
                        user.getId(),
                        user.getNickname(),
                        user.getAvatar(),
                        user.getSignature(),
                        user.getCampus(),
                        loginId != null && !Objects.equals(loginId, user.getId()) && followed.contains(user.getId())
                ))
                .toList();
    }

    private void assertAppUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(0).equals(user.getStatus())) {
            throw new BizException("用户不存在");
        }
        SysRole role = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, BookConstants.ROLE_APP_USER), false);
        if (role == null) {
            throw new BizException("用户不存在");
        }
        Long count = userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, role.getId()));
        if (count == null || count == 0) {
            throw new BizException("用户不存在");
        }
    }
}
