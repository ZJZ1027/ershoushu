package com.basepro.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.basepro.common.BizException;
import com.basepro.common.ErrorCode;
import com.basepro.config.AppProperties;
import com.basepro.system.dto.TokenVO;
import com.basepro.system.entity.SysToken;
import com.basepro.system.mapper.SysTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 令牌的签发与刷新。访问令牌是自包含的 JWT，刷新令牌落库以便登出时吊销。
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    /**
     * 刷新失败一律用同一句提示：令牌不存在、已过期、用户被停用对调用方是一回事，
     * 都只能重新登录；区分开反而会泄露令牌是否存在
     */
    private static final String INVALID_REFRESH_TOKEN = "无效的刷新令牌";

    private final JwtEncoder jwtEncoder;
    private final SysTokenMapper tokenMapper;
    private final LoginUserService loginUserService;
    private final AppProperties properties;

    @Transactional(rollbackFor = Exception.class)
    public TokenVO create(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.jwt().accessTokenExpire());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(loginUser.userId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("username", loginUser.username())
                .claim("tenantId", loginUser.tenantId())
                .build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        // 清理该用户已过期的刷新令牌，避免表无限膨胀
        tokenMapper.delete(Wrappers.<SysToken>lambdaQuery()
                .eq(SysToken::getUserId, loginUser.userId())
                .lt(SysToken::getExpiresTime, LocalDateTime.now()));

        SysToken token = new SysToken();
        token.setTenantId(loginUser.tenantId());
        token.setUserId(loginUser.userId());
        token.setRefreshToken(UUID.randomUUID().toString().replace("-", ""));
        token.setExpiresTime(LocalDateTime.now().plus(properties.jwt().refreshTokenExpire()));
        token.setCreateTime(LocalDateTime.now());
        tokenMapper.insert(token);

        return new TokenVO(accessToken, token.getRefreshToken(), loginUser.userId(), loginUser.tenantId(),
                expiresAt.toEpochMilli());
    }

    @Transactional(rollbackFor = Exception.class)
    public TokenVO refresh(String refreshToken) {
        SysToken token = tokenMapper.selectOne(Wrappers.<SysToken>lambdaQuery()
                .eq(SysToken::getRefreshToken, refreshToken), false);
        if (token == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), INVALID_REFRESH_TOKEN);
        }
        tokenMapper.deleteById(token.getId());
        if (token.getExpiresTime().isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), INVALID_REFRESH_TOKEN);
        }
        LoginUser loginUser = loginUserService.loadById(token.getUserId());
        if (loginUser == null || !loginUser.isEnabled()) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), INVALID_REFRESH_TOKEN);
        }
        return create(loginUser);
    }

    /**
     * 登出：吊销该用户的全部刷新令牌
     */
    public void revoke(Long userId) {
        tokenMapper.delete(Wrappers.<SysToken>lambdaQuery().eq(SysToken::getUserId, userId));
    }

}
