package com.basepro.business.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.business.BookConstants;
import com.basepro.business.dto.AdminBadgeVO;
import com.basepro.business.dto.DashboardVO;
import com.basepro.business.dto.MemberQuery;
import com.basepro.business.entity.BuBook;
import com.basepro.business.entity.BuBookOrder;
import com.basepro.business.entity.BuInquiry;
import com.basepro.business.entity.BuReport;
import com.basepro.business.mapper.BuBookMapper;
import com.basepro.business.mapper.BuBookOrderMapper;
import com.basepro.business.mapper.BuInquiryMapper;
import com.basepro.business.mapper.BuReportMapper;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.security.LoginUserService;
import com.basepro.system.entity.SysRole;
import com.basepro.system.entity.SysUser;
import com.basepro.system.entity.SysUserRole;
import com.basepro.system.mapper.SysRoleMapper;
import com.basepro.system.mapper.SysUserMapper;
import com.basepro.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuMemberService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final LoginUserService loginUserService;
    private final BuBookMapper bookMapper;
    private final BuBookOrderMapper orderMapper;
    private final BuReportMapper reportMapper;
    private final BuInquiryMapper inquiryMapper;

    public PageResult<SysUser> page(MemberQuery query) {
        Long roleId = appRoleId();
        List<Long> userIds = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getRoleId, roleId))
                .stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return PageResult.empty();
        }
        Page<SysUser> page = userMapper.selectPage(query.toPage(), Wrappers.<SysUser>lambdaQuery()
                .in(SysUser::getId, userIds)
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getNickname()), SysUser::getNickname, query.getNickname())
                .like(StringUtils.hasText(query.getMobile()), SysUser::getMobile, query.getMobile())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByDesc(SysUser::getId));
        return PageResult.of(page);
    }

    public void updateStatus(Long id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(status);
        userMapper.updateById(update);
        loginUserService.evict(id);
    }

    public DashboardVO dashboard() {
        long pendingBook = bookMapper.selectCount(Wrappers.<BuBook>lambdaQuery()
                .eq(BuBook::getStatus, BookConstants.BOOK_PENDING));
        long onSale = bookMapper.selectCount(Wrappers.<BuBook>lambdaQuery()
                .eq(BuBook::getStatus, BookConstants.BOOK_ON_SALE));
        long reserved = bookMapper.selectCount(Wrappers.<BuBook>lambdaQuery()
                .eq(BuBook::getStatus, BookConstants.BOOK_RESERVED));
        long pendingOrder = orderMapper.selectCount(Wrappers.<BuBookOrder>lambdaQuery()
                .eq(BuBookOrder::getStatus, BookConstants.ORDER_PENDING));
        long pendingReport = reportMapper.selectCount(Wrappers.<BuReport>lambdaQuery()
                .eq(BuReport::getStatus, BookConstants.REPORT_PENDING));
        Long roleId = appRoleId();
        long members = userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getRoleId, roleId));
        return new DashboardVO(pendingBook, onSale, reserved, pendingOrder, pendingReport, members);
    }

    public AdminBadgeVO badges(Long memberSinceMillis) {
        long book = bookMapper.selectCount(Wrappers.<BuBook>lambdaQuery()
                .eq(BuBook::getStatus, BookConstants.BOOK_PENDING));
        long order = orderMapper.selectCount(Wrappers.<BuBookOrder>lambdaQuery()
                .eq(BuBookOrder::getStatus, BookConstants.ORDER_PENDING));
        long inquiry = inquiryMapper.selectCount(Wrappers.<BuInquiry>lambdaQuery()
                .eq(BuInquiry::getAdminUnread, 1));
        long report = reportMapper.selectCount(Wrappers.<BuReport>lambdaQuery()
                .eq(BuReport::getStatus, BookConstants.REPORT_PENDING));
        long member = 0;
        if (memberSinceMillis != null && memberSinceMillis > 0) {
            LocalDateTime since = LocalDateTime.ofInstant(Instant.ofEpochMilli(memberSinceMillis), ZoneId.systemDefault());
            Long roleId = appRoleId();
            List<Long> userIds = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                            .eq(SysUserRole::getRoleId, roleId))
                    .stream()
                    .map(SysUserRole::getUserId)
                    .distinct()
                    .toList();
            if (!userIds.isEmpty()) {
                member = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                        .in(SysUser::getId, userIds)
                        .gt(SysUser::getCreateTime, since));
            }
        }
        return new AdminBadgeVO(book, order, inquiry, member, report);
    }

    private Long appRoleId() {
        SysRole role = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, BookConstants.ROLE_APP_USER), false);
        if (role == null) {
            throw new BizException("未配置校园用户角色");
        }
        return role.getId();
    }

}
