package com.basepro.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.system.entity.SysUser;
import com.basepro.system.mapper.SysMenuMapper;
import com.basepro.system.mapper.SysRoleMapper;
import com.basepro.system.mapper.SysUserMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 登录用户的加载：
 * <ul>
 *     <li>{@link #loadUserByUsername(String)}：账号密码登录时使用，Spring Security 调用；</li>
 *     <li>{@link #loadById(Long)}：携带令牌的请求鉴权时使用，本地缓存 1 分钟。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class LoginUserService implements UserDetailsService {

    /**
     * 权限缓存时长：改授权后最迟 1 分钟生效，也可调用 {@link #evict(Long)} 立即失效
     */
    private static final Duration CACHE_TTL = Duration.ofMinutes(1);

    /**
     * 前端 v-hasPermi 指令约定的“全部权限”标识
     */
    private static final String ALL_PERMISSION = "*:*:*";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    private final Cache<Long, LoginUser> cache = Caffeine.newBuilder()
            .expireAfterWrite(CACHE_TTL)
            .maximumSize(10_000)
            .build();

    @Override
    public LoginUser loadUserByUsername(String username) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username), false);
        if (user == null) {
            throw new UsernameNotFoundException("账号不存在");
        }
        return build(user);
    }

    public LoginUser loadById(Long userId) {
        return cache.get(userId, id -> {
            SysUser user = userMapper.selectByIdIgnoreTenant(id);
            return user == null ? null : build(user);
        });
    }

    public void evict(Long userId) {
        cache.invalidate(userId);
    }

    /**
     * 菜单、角色授权发生变化时清空，保证权限立即生效
     */
    public void evictAll() {
        cache.invalidateAll();
    }

    private LoginUser build(SysUser user) {
        List<String> roles = roleMapper.selectCodesByUserId(user.getId());
        List<String> permissions = new ArrayList<>(roles.contains(LoginUser.SUPER_ADMIN)
                ? menuMapper.selectAllPermissions()
                : menuMapper.selectPermissionsByUserId(user.getId()));
        if (roles.contains(LoginUser.SUPER_ADMIN)) {
            permissions.add(ALL_PERMISSION);
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(), user.getNickname(),
                user.getTenantId(), user.getDeptId(), Integer.valueOf(0).equals(user.getStatus()),
                roles, permissions);
    }

}
