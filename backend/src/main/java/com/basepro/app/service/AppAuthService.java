package com.basepro.app.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.app.dto.AppProfileUpdateReq;
import com.basepro.app.dto.AppProfileVO;
import com.basepro.app.dto.RegisterReq;
import com.basepro.business.BookConstants;
import com.basepro.common.BizException;
import com.basepro.security.LoginUserService;
import com.basepro.security.SecurityUtils;
import com.basepro.system.dto.LoginReq;
import com.basepro.system.dto.TokenVO;
import com.basepro.system.entity.SysRole;
import com.basepro.system.entity.SysUser;
import com.basepro.system.entity.SysUserRole;
import com.basepro.system.mapper.SysRoleMapper;
import com.basepro.system.mapper.SysUserMapper;
import com.basepro.system.mapper.SysUserRoleMapper;
import com.basepro.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AppAuthService {

    private final AuthService authService;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginUserService loginUserService;

    @Transactional(rollbackFor = Exception.class)
    public TokenVO register(RegisterReq req) {
        Long exists = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, req.username()));
        if (exists > 0) {
            throw new BizException("账号已存在");
        }
        if (StringUtils.hasText(req.mobile())) {
            Long mobileCount = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getMobile, req.mobile()));
            if (mobileCount > 0) {
                throw new BizException("手机号已存在");
            }
        }
        SysRole role = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, BookConstants.ROLE_APP_USER), false);
        if (role == null) {
            throw new BizException("系统未配置校园用户角色");
        }
        SysUser user = new SysUser();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(req.nickname());
        user.setMobile(req.mobile());
        user.setCampus(req.campus());
        user.setStatus(0);
        userMapper.insert(user);
        userRoleMapper.insert(SysUserRole.of(user.getId(), role.getId()));
        return authService.login(new LoginReq(req.username(), req.password(), null));
    }

    public TokenVO login(LoginReq req) {
        return authService.login(req);
    }

    public AppProfileVO profile() {
        SysUser user = current();
        return new AppProfileVO(user.getId(), user.getUsername(), user.getNickname(),
                user.getMobile(), user.getWechat(), user.getCampus(), user.getAvatar(),
                user.getSex(), user.getCreateTime());
    }

    public void updateProfile(AppProfileUpdateReq req) {
        Long userId = SecurityUtils.getUserId();
        SysUser update = new SysUser();
        update.setId(userId);
        if (StringUtils.hasText(req.nickname())) {
            update.setNickname(req.nickname());
        }
        if (StringUtils.hasText(req.mobile())) {
            Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getMobile, req.mobile())
                    .ne(SysUser::getId, userId));
            if (count > 0) {
                throw new BizException("手机号已存在");
            }
            update.setMobile(req.mobile());
        }
        if (req.wechat() != null) {
            update.setWechat(req.wechat());
        }
        if (req.campus() != null) {
            update.setCampus(req.campus());
        }
        if (req.sex() != null) {
            update.setSex(req.sex());
        }
        if (StringUtils.hasText(req.avatar())) {
            update.setAvatar(req.avatar());
        }
        userMapper.updateById(update);
        loginUserService.evict(userId);
    }

    private SysUser current() {
        SysUser user = userMapper.selectById(SecurityUtils.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

}
