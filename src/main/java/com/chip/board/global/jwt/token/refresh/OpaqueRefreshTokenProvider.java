package com.chip.board.global.jwt.token.refresh;

import com.chip.board.global.jwt.JwtClaims;
import com.chip.board.global.jwt.properties.RefreshTokenProperties;

import java.security.SecureRandom;
import java.util.Base64;

public class OpaqueRefreshTokenProvider implements RefreshTokenProvider {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public OpaqueRefreshTokenProvider(RefreshTokenProperties props) {
    }

    @Override
    public RefreshTokenData createToken(JwtClaims claims) {
        byte[] bytes = new byte[64]; // 512 bits
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        return new RefreshTokenData(token, claims.userId());
    }
}