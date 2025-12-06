package com.chip.board.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    private static final String DEFAULT_PATH = "/";
    private static final boolean DEFAULT_HTTP_ONLY = true;
    private static final boolean DEFAULT_SECURE = true;     // 개발 중이면 false로 바꿔도 됨
    private static final String DEFAULT_SAME_SITE = "Strict";

    /**
     * 요청에서 쿠키 값 조회
     */
    public Optional<String> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Response에 추가할 쿠키 생성
     */
    public ResponseCookie addCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(DEFAULT_HTTP_ONLY)
                .secure(DEFAULT_SECURE)
                .sameSite(DEFAULT_SAME_SITE)
                .path(DEFAULT_PATH)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }

    /**
     * 삭제용 쿠키 (maxAge 0) 생성
     */
    public ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(DEFAULT_HTTP_ONLY)
                .secure(DEFAULT_SECURE)
                .sameSite(DEFAULT_SAME_SITE)
                .path(DEFAULT_PATH)
                .maxAge(0)
                .build();
    }
}
