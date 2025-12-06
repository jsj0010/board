package com.chip.board.global.oauth.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.global.jwt.properties.RefreshTokenProperties;
import com.chip.board.global.jwt.token.access.AccessTokenData;
import com.chip.board.global.jwt.token.refresh.RefreshTokenData;
import com.chip.board.global.oauth.dto.response.TokenPair;
import com.chip.board.global.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthHttpResponseMapper {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final CookieUtil cookieUtil;
    private final RefreshTokenProperties refreshTokenProperties;

    public ResponseEntity<ResponseBody<AccessTokenData>> toLoginResponse(TokenPair pair) {
        AccessTokenData accessToken = pair.accessToken();
        RefreshTokenData refreshToken = pair.refreshToken();

        // CookieUtil 사용
        ResponseCookie refreshCookie =
                cookieUtil.addCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken.token(), refreshTokenProperties.ttlSeconds());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ResponseUtils.createSuccessResponse(accessToken));
    }
}
