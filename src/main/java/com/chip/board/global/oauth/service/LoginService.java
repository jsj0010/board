package com.chip.board.global.oauth.service;

import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.jwt.JwtClaims;
import com.chip.board.global.jwt.token.access.AccessTokenData;
import com.chip.board.global.jwt.token.access.AccessTokenProvider;
import com.chip.board.global.jwt.token.refresh.RefreshTokenData;
import com.chip.board.global.jwt.token.refresh.RefreshTokenProvider;
import com.chip.board.register.domain.CustomUserDetails;
import com.chip.board.register.domain.Role;
import com.chip.board.global.oauth.dto.request.LoginRequest;
import com.chip.board.global.oauth.dto.response.TokenPair;
import com.chip.board.register.domain.User;
import com.chip.board.global.oauth.repository.RefreshTokenRepository;
import com.chip.board.register.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenPair login(LoginRequest request) {
        try {
            // 1) 스프링 시큐리티에 인증 위임
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            Long userId = principal.getId();
            Role role = principal.getRole();

            refreshTokenRepository.deleteAllByUserId(userId);

            JwtClaims claims = new JwtClaims(userId, role);

            AccessTokenData accessToken = accessTokenProvider.createToken(claims);
            RefreshTokenData refreshToken = refreshTokenProvider.createToken(claims);
            refreshTokenRepository.save(refreshToken);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
            user.onLoginSuccess();

            return new TokenPair(accessToken, refreshToken);

        } catch (AuthenticationException e) {
            // UsernameNotFoundException, BadCredentialsException 등 모두 포함
            throw new ServiceException(ErrorCode.INVALID_LOGIN);
        }
    }
}
