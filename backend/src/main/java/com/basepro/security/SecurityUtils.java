package com.basepro.security;

import com.basepro.common.BizException;
import com.basepro.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户的读取入口。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * @return 当前登录用户，未登录返回 null
     */
    public static LoginUser getLoginUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
    }

    /**
     * @return 当前登录用户，未登录抛出 401
     */
    public static LoginUser getLoginUser() {
        LoginUser loginUser = getLoginUserOrNull();
        if (loginUser == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    public static Long getUserId() {
        return getLoginUser().userId();
    }

    /**
     * @return 当前登录用户名，未登录返回 null（用于审计字段填充）
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUserOrNull();
        return loginUser == null ? null : loginUser.username();
    }

}
