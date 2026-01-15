package com.chip.board.oauth.application.service;

import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.jwt.JwtClaims;
import com.chip.board.global.jwt.token.access.AccessTokenData;
import com.chip.board.global.jwt.token.access.AccessTokenProvider;
import com.chip.board.global.jwt.token.refresh.RefreshTokenData;
import com.chip.board.global.jwt.token.refresh.RefreshTokenProvider;
import com.chip.board.oauth.application.component.port.LoginAuthenticator;
import com.chip.board.register.application.component.reader.UserFinder;
import com.chip.board.oauth.application.component.writer.RefreshTokenWriter;
import com.chip.board.oauth.application.component.writer.UserWriter;
import com.chip.board.register.domain.CustomUserDetails;
import com.chip.board.register.domain.Role;
import com.chip.board.oauth.presentation.dto.request.LoginRequest;
import com.chip.board.global.jwt.dto.response.TokenPair;
import com.chip.board.register.domain.User;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginAuthenticator loginAuthenticator;
    private final UserFinder userFinder;
    private final UserWriter userWriter;
    private final RefreshTokenWriter refreshTokenWriter;

    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;

    @Transactional
    public TokenPair login(LoginRequest request) {
        try {
            CustomUserDetails principal = loginAuthenticator.authenticate(request);

            Long userId = principal.getId();
            Role role = principal.getRole();

            JwtClaims claims = new JwtClaims(userId, role);

            AccessTokenData accessToken = accessTokenProvider.createToken(claims);
            RefreshTokenData refreshToken = refreshTokenProvider.createToken(claims);

            // TODO 여러기기 로그인되도록 수정 고려
            refreshTokenWriter.replaceUserToken(userId, refreshToken);

            User user = userFinder.findById(userId);
            userWriter.onLoginSuccess(user);

            return new TokenPair(accessToken, refreshToken);

        } catch (AuthenticationException e) {
            throw new ServiceException(ErrorCode.INVALID_LOGIN);
        }
    }
}
