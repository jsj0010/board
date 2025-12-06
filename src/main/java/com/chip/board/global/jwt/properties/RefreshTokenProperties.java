package com.chip.board.global.jwt.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "refresh-token")
public record RefreshTokenProperties(
        long ttlSeconds  // 예: 1209600 (14일)
) {}