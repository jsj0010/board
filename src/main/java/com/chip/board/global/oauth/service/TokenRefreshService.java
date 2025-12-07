package com.chip.board.global.oauth.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.global.jwt.JwtClaims;
import com.chip.board.global.jwt.token.access.AccessTokenData;
import com.chip.board.global.jwt.token.access.AccessTokenProvider;
import com.chip.board.global.jwt.token.refresh.RefreshTokenData;
import com.chip.board.global.jwt.token.refresh.RefreshTokenProvider;
import com.chip.board.global.oauth.dto.response.TokenPair;
import com.chip.board.global.oauth.repository.RefreshTokenRepository;
import com.chip.board.register.domain.User;
import com.chip.board.register.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final UserRepository userRepository;

    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ServiceException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 1) RT로 userId 조회
        Long userId = refreshTokenRepository.findUserIdByToken(rawRefreshToken)
                .orElseThrow(() -> new ServiceException(ErrorCode.REFRESH_TOKEN_INVALID));

        // 2) RTR: 기존 RT 삭제
        refreshTokenRepository.deleteByToken(rawRefreshToken);

        // 3) user 로드 (JwtClaims 생성용)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        JwtClaims claims = JwtClaims.create(user);

        // 4) 새 AT / RT 발급
        AccessTokenData newAccessToken = accessTokenProvider.createToken(claims);
        RefreshTokenData newRefreshToken = refreshTokenProvider.createToken(claims);

        // 5) 새 RT 저장
        refreshTokenRepository.save(newRefreshToken);

        return new TokenPair(newAccessToken, newRefreshToken);
    }
}
