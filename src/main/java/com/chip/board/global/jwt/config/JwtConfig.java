package com.chip.board.global.jwt.config;

import com.chip.board.global.jwt.authentication.JwtAuthenticationProvider;
import com.chip.board.global.jwt.authentication.UsernamePasswordAuthenticationProvider;
import com.chip.board.global.jwt.properties.JwtProperties;
import com.chip.board.global.jwt.properties.RefreshTokenProperties;
import com.chip.board.global.jwt.token.access.AccessTokenProvider;
import com.chip.board.global.jwt.token.refresh.OpaqueRefreshTokenProvider;
import com.chip.board.global.jwt.token.refresh.RefreshTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

import java.util.List;

@Configuration
@EnableConfigurationProperties({ JwtProperties.class, RefreshTokenProperties.class })
public class JwtConfig {

    @Bean
    public AccessTokenProvider accessTokenProvider(JwtProperties jwtProperties) {
        return new AccessTokenProvider(jwtProperties);
    }

    @Bean
    public RefreshTokenProvider refreshTokenProvider(RefreshTokenProperties refreshProps) {
        return new OpaqueRefreshTokenProvider(refreshProps);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider,
            JwtAuthenticationProvider jwtAuthenticationProvider
    ) {
        return new ProviderManager(
                List.of(
                        usernamePasswordAuthenticationProvider,
                        jwtAuthenticationProvider
                )
        );
    }
}

