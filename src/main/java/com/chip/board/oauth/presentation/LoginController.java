package com.chip.board.oauth.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.jwt.token.access.AccessTokenData;
import com.chip.board.oauth.presentation.dto.request.LoginRequest;
import com.chip.board.global.jwt.dto.response.TokenPair;
import com.chip.board.oauth.application.service.LoginService;
import com.chip.board.oauth.application.service.TokenRefreshService;
import com.chip.board.oauth.presentation.mapper.AuthTokenResponseMapper;
import com.chip.board.oauth.presentation.swagger.LoginSwagger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController implements LoginSwagger {

    private final LoginService loginService;
    private final AuthTokenResponseMapper authTokenResponseMapper;
    private final TokenRefreshService tokenRefreshService;

    @PostMapping("/login")
    public ResponseEntity<ResponseBody<AccessTokenData>> login(@RequestBody LoginRequest request) {
        TokenPair pair = loginService.login(request);
        return authTokenResponseMapper.toTokenPairResponse(pair);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseBody<AccessTokenData>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        TokenPair pair = tokenRefreshService.refresh(refreshToken);
        return authTokenResponseMapper.toTokenPairResponse(pair);
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseBody<Void>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        tokenRefreshService.logout(refreshToken);
        return authTokenResponseMapper.toLogoutResponse();
    }
}