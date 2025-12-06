package com.chip.board.global.jwt.authentication;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.global.jwt.JwtClaims;
import com.chip.board.global.jwt.token.access.AccessTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import io.jsonwebtoken.security.SignatureException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final AccessTokenProvider accessTokenProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtUnauthenticatedToken unauthenticatedToken = (JwtUnauthenticatedToken) authentication;

        String accessToken = unauthenticatedToken.token();
        if (!StringUtils.hasText(accessToken)) {
            return null;
        }

        try {
            JwtClaims claims = accessTokenProvider.parseToken(accessToken);
            return new JwtAuthentication(claims);
        } catch (ExpiredJwtException e) {
            throw new ServiceException(ErrorCode.JWT_EXPIRED);
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            throw new ServiceException(ErrorCode.JWT_INVALID);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtUnauthenticatedToken.class.isAssignableFrom(authentication);
    }
}
