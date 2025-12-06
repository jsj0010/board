package com.chip.board.global.jwt.token.refresh;

import com.chip.board.global.jwt.JwtClaims;

public interface RefreshTokenProvider {
    RefreshTokenData createToken(JwtClaims claims);
}