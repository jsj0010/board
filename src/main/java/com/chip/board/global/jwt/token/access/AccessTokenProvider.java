package com.chip.board.global.jwt.token.access;

import com.chip.board.global.jwt.JwtClaims;
import com.chip.board.global.jwt.properties.JwtProperties;
import com.chip.board.register.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

public class AccessTokenProvider {
    private final Key key;
    private final String issuer;
    private final long accessTtlSeconds;

    public AccessTokenProvider(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.issuer = props.issuer();
        this.accessTtlSeconds = props.accessTtlSeconds();
    }

    public AccessTokenData createToken(JwtClaims claims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);

        String jwt = Jwts.builder()
                .setIssuer(issuer)
                .setSubject(String.valueOf(claims.userId()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("typ", "access")
                .claim("role", claims.role().name())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();


        return new AccessTokenData(jwt, exp);
    }
    public JwtClaims parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)          // 토큰 생성 때 사용한 SecretKey
                .requireIssuer(issuer)       // issuer 검증
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.valueOf(claims.getSubject());
        String roleName = claims.get("role", String.class);
        Role role = Role.valueOf(roleName);

        return new JwtClaims(userId, role);
    }

}
