package com.chip.board.global.jwt.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,         // 최소 256-bit (32 bytes) 이상
        String issuer,         // 예: "https://api.example.com"
        long accessTtlSeconds  // 예: 900 (15분)
) {}