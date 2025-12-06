package com.chip.board.global.oauth.dto.response;

import com.chip.board.global.jwt.token.access.AccessTokenData;
import com.chip.board.global.jwt.token.refresh.RefreshTokenData;

public record TokenPair(
        AccessTokenData accessToken,
        RefreshTokenData refreshToken
) {}

