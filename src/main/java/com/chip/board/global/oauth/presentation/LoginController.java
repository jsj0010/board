package com.chip.board.global.oauth.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.jwt.token.access.AccessTokenData;
import com.chip.board.global.oauth.dto.request.LoginRequest;
import com.chip.board.global.oauth.dto.response.TokenPair;
import com.chip.board.global.oauth.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final AuthHttpResponseMapper authHttpResponseMapper;

    @PostMapping("/login")
    public ResponseEntity<ResponseBody<AccessTokenData>> login(@RequestBody LoginRequest request) {
        TokenPair pair = loginService.login(request);
        return authHttpResponseMapper.toLoginResponse(pair);
    }
}