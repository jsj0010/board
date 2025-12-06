package com.chip.board.global.jwt.token.access;

import java.time.Instant;

public record AccessTokenData(
        String token,
        Instant expiredIn
){
}

