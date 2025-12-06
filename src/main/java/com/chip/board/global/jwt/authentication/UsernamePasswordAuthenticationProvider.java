package com.chip.board.global.jwt.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService; // CustomUserDetailsService 주입됨
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // AuthenticationManager가 넘겨준 토큰에서 값 추출
        String username = (String) authentication.getPrincipal();
        String rawPassword = (String) authentication.getCredentials();

        // 1) 사용자 조회
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // 3) 인증 성공 토큰 생성
        // credentials는 보안상 null 처리 권장
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // UsernamePasswordAuthenticationToken만 처리
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

