package com.chip.board.oauth.application.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.global.jwt.JwtClaims;
import com.chip.board.global.jwt.token.access.AccessTokenData;
import com.chip.board.global.jwt.token.access.AccessTokenProvider;
import com.chip.board.global.jwt.token.refresh.RefreshTokenData;
import com.chip.board.global.jwt.token.refresh.RefreshTokenProvider;
import com.chip.board.oauth.application.component.reader.RefreshTokenFinder;
import com.chip.board.oauth.application.component.writer.RefreshTokenWriter;
import com.chip.board.global.jwt.dto.response.TokenPair;
import com.chip.board.register.domain.User;
import com.chip.board.register.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final RefreshTokenFinder refreshTokenFinder;
    private final RefreshTokenWriter refreshTokenWriter;

    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;

    private final UserRepository userRepository;

    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        validateRawRefreshToken(rawRefreshToken);

        Long userId = refreshTokenFinder.findUserIdByTokenOrThrow(rawRefreshToken);

        refreshTokenWriter.deleteByToken(rawRefreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        JwtClaims claims = JwtClaims.create(user);

        AccessTokenData newAccessToken = accessTokenProvider.createToken(claims);
        RefreshTokenData newRefreshToken = refreshTokenProvider.createToken(claims);

        refreshTokenWriter.save(newRefreshToken);

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenWriter.deleteByToken(rawRefreshToken);
    }

    private void validateRawRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ServiceException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }
}
