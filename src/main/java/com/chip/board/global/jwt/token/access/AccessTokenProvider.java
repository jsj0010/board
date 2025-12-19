package com.chip.board.global.jwt.token.access;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.global.jwt.JwtClaims;
import com.chip.board.global.jwt.properties.JwtProperties;
import com.chip.board.register.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class AccessTokenProvider {
    private final SecretKey secretKey;
    private final String issuer;
    private final long accessTtlSeconds;

    public AccessTokenProvider(JwtProperties props) {
        byte[] secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) { // 32 bytes = 256 bits
            throw new IllegalArgumentException("JWT secret key must be at least 256 bits (32 bytes) long for HS256 algorithm.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.issuer = props.issuer();
        this.accessTtlSeconds = props.accessTtlSeconds();
    }

    public AccessTokenData createToken(JwtClaims claims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);

        String jwt = Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(claims.userId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("typ", "access")
                .claim("role", claims.role().name())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        return new AccessTokenData(jwt, exp);
    }

    public JwtClaims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .require("typ", "access")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.getSubject());
            Role role = Role.valueOf(claims.get("role", String.class));
            return new JwtClaims(userId, role);

        } catch (ExpiredJwtException e) {
            throw new ServiceException(ErrorCode.JWT_EXPIRED);

        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: 서명/형식/지원 알고리즘/클레임 검증 등
            // IllegalArgumentException: token null/blank 등 일부 케이스
            throw new ServiceException(ErrorCode.JWT_INVALID);
        }
    }
}