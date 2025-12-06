package com.chip.board.global.jwt.authentication;

import com.chip.board.global.jwt.JwtClaims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.Collections;

public record JwtAuthentication (
        Long userId
) implements Authentication {

    public JwtAuthentication(JwtClaims claims) {
        this(claims.userId());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return null;
    }
}
