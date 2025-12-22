package com.chip.board.global.jwt.authentication;

import com.chip.board.global.jwt.JwtClaims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record JwtAuthentication(Long userId, String role) implements Authentication {

    public JwtAuthentication(JwtClaims claims) {
        this(claims.userId(), claims.role().name());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //@PreAuthorize("hasRole('ADMIN')")으로 받음
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public Object getPrincipal() { return userId; }
    @Override public Object getCredentials() { return null; }
    @Override public Object getDetails() { return null; }
    @Override public boolean isAuthenticated() { return true; }
    @Override public void setAuthenticated(boolean isAuthenticated) { }
    @Override public String getName() { return Objects.toString(userId, null); }
}