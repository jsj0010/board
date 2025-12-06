package com.chip.board.register.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // ===== 권한 =====
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    // ===== 인증 정보 =====
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // ===== 계정 상태 (필요하면 Account에 필드 추가해서 실제 값 반영) =====
    @Override
    public boolean isAccountNonExpired() {
        return true;    // 만료 개념 없으면 일단 true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;    // 잠금 필드 없으니 true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;    // 비밀번호 만료 정책 없으면 true
    }

    @Override
    public boolean isEnabled() {
        return true;    // 탈퇴/비활성화 필드 없으면 true
    }

    public Long getId() {
        return user.getId();
    }

    public Role getRole() {
        return user.getRole();
    }
}