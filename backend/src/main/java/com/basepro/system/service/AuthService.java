package com.basepro.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.common.BizException;
import com.basepro.common.WebUtils;
import com.basepro.security.LoginUser;
import com.basepro.security.LoginUserService;
import com.basepro.security.SecurityUtils;
import com.basepro.security.TokenService;
import com.basepro.system.dto.LoginReq;
import com.basepro.system.dto.PermissionInfoVO;
import com.basepro.system.dto.TokenVO;
import com.basepro.system.entity.SysLoginLog;
import com.basepro.system.entity.SysMenu;
import com.basepro.system.entity.SysTenant;
import com.basepro.system.entity.SysUser;
import com.basepro.system.mapper.SysLoginLogMapper;
import com.basepro.system.mapper.SysMenuMapper;
import com.basepro.system.mapper.SysTenantMapper;
import com.basepro.system.mapper.SysUserMapper;
import com.basepro.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 登录、登出、令牌刷新与登录后的权限信息。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final LoginUserService loginUserService;
    private final SysUserMapper userMapper;
    private final SysTenantMapper tenantMapper;
    private final SysMenuMapper menuMapper;
    private final SysLoginLogMapper loginLogMapper;

    public TokenVO login(LoginReq request) {
        resolveTenant(request.tenantName());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            recordLoginInfo(loginUser.userId());
            saveLoginLog(SysLoginLog.TYPE_LOGIN, loginUser.userId(), request.username(), SysLoginLog.RESULT_SUCCESS);
            // 登录信息变化，清掉缓存
            loginUserService.evict(loginUser.userId());
            return tokenService.create(loginUser);
        } catch (DisabledException e) {
            saveLoginLog(SysLoginLog.TYPE_LOGIN, null, request.username(), SysLoginLog.RESULT_DISABLED);
            throw new BizException("账号已被停用");
        } catch (AuthenticationException e) {
            saveLoginLog(SysLoginLog.TYPE_LOGIN, null, request.username(), SysLoginLog.RESULT_BAD_CREDENTIALS);
            throw new BizException("账号或密码不正确");
        }
    }

    public TokenVO refreshToken(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    public void logout() {
        LoginUser loginUser = SecurityUtils.getLoginUserOrNull();
        if (loginUser == null) {
            return;
        }
        tokenService.revoke(loginUser.userId());
        loginUserService.evict(loginUser.userId());
        saveLoginLog(SysLoginLog.TYPE_LOGOUT, loginUser.userId(), loginUser.username(), SysLoginLog.RESULT_SUCCESS);
    }

    public PermissionInfoVO getPermissionInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = userMapper.selectById(loginUser.userId());
        List<SysMenu> menus = loginUser.isSuperAdmin()
                ? menuMapper.selectAllMenus()
                : menuMapper.selectMenusByUserId(loginUser.userId());
        PermissionInfoVO.UserInfo userInfo = new PermissionInfoVO.UserInfo(user.getId(), user.getUsername(),
                user.getNickname(), user.getAvatar(), user.getDeptId());
        return new PermissionInfoVO(userInfo, loginUser.roles(), loginUser.permissions(), buildMenuTree(menus));
    }

    /**
     * 指定了租户名则切换租户上下文，否则沿用 tenant-id 请求头 / 默认租户
     */
    private void resolveTenant(String tenantName) {
        if (tenantName == null || tenantName.isBlank()) {
            return;
        }
        SysTenant tenant = TenantContext.ignoreTenant(() -> tenantMapper.selectOne(
                Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getName, tenantName), false));
        if (tenant == null) {
            throw new BizException("租户不存在");
        }
        if (!Integer.valueOf(0).equals(tenant.getStatus())) {
            throw new BizException("租户已被停用");
        }
        if (tenant.getExpireTime() != null && tenant.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BizException("租户已过期");
        }
        TenantContext.setTenantId(tenant.getId());
    }

    private void recordLoginInfo(Long userId) {
        SysUser update = new SysUser();
        update.setId(userId);
        update.setLoginIp(WebUtils.getClientIp());
        update.setLoginDate(LocalDateTime.now());
        userMapper.updateById(update);
    }

    private void saveLoginLog(int logType, Long userId, String username, int result) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setLogType(logType);
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setResult(result);
        loginLog.setUserIp(WebUtils.getClientIp());
        loginLog.setUserAgent(WebUtils.getUserAgent());
        loginLog.setCreateTime(LocalDateTime.now());
        loginLogMapper.insert(loginLog);
    }

    /**
     * 前端路由要求菜单以树形返回
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        Map<Long, SysMenu> menuMap = menus.stream().collect(Collectors.toMap(SysMenu::getId, Function.identity()));
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            SysMenu parent = menuMap.get(menu.getParentId());
            if (parent == null) {
                roots.add(menu);
                continue;
            }
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(menu);
        }
        return roots;
    }

}
