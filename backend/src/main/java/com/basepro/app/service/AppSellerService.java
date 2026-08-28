package com.basepro.app.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.app.dto.SellerPublicVO;
import com.basepro.business.BookConstants;
import com.basepro.business.entity.BuBook;
import com.basepro.business.mapper.BuBookMapper;
import com.basepro.common.BizException;
import com.basepro.security.SecurityUtils;
import com.basepro.system.entity.SysRole;
import com.basepro.system.entity.SysUser;
import com.basepro.system.entity.SysUserRole;
import com.basepro.system.mapper.SysRoleMapper;
import com.basepro.system.mapper.SysUserMapper;
import com.basepro.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppSellerService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final BuBookMapper bookMapper;
    private final AppFollowService followService;

    public SellerPublicVO getPublicProfile(Long sellerId) {
        SysUser user = userMapper.selectById(sellerId);
        if (user == null || !Integer.valueOf(0).equals(user.getStatus())) {
            throw new BizException("用户不存在");
        }
        SysRole role = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, BookConstants.ROLE_APP_USER), false);
        if (role == null) {
            throw new BizException("用户不存在");
        }
        Long count = userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, sellerId)
                .eq(SysUserRole::getRoleId, role.getId()));
        if (count == null || count == 0) {
            throw new BizException("用户不存在");
        }
        Long onSaleCount = bookMapper.selectCount(Wrappers.<BuBook>lambdaQuery()
                .eq(BuBook::getSellerId, sellerId)
                .in(BuBook::getStatus, BookConstants.BOOK_ON_SALE, BookConstants.BOOK_RESERVED));
        Long loginId = SecurityUtils.getLoginUserOrNull() == null ? null : SecurityUtils.getUserId();
        boolean isFollowing = loginId != null && followService.isFollowing(loginId, sellerId);
        return new SellerPublicVO(
                user.getId(),
                user.getNickname(),
                user.getAvatar(),
                user.getSignature(),
                user.getCampus(),
                onSaleCount == null ? 0L : onSaleCount,
                followService.countFollowers(sellerId),
                followService.countFollowing(sellerId),
                isFollowing
        );
    }
}
