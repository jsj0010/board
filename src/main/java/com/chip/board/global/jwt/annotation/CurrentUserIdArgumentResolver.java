package com.chip.board.global.jwt.annotation;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.global.jwt.authentication.JwtAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class) && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        CurrentUserId annotation = parameter.getParameterAnnotation(CurrentUserId.class);
        boolean required = annotation == null || annotation.required();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthentication jwtAuthentication) {
            Object principal = jwtAuthentication.getPrincipal();
            if (principal != null) return principal;
        }

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            if (required) {
                throw new ServiceException(ErrorCode.NEED_AUTHORIZED);
            }
            return null;
        }

        log.error("알 수 없는 Authentication 타입입니다: {}", authentication.getClass());
        throw new IllegalArgumentException("@CurrentUserId는 JWT 기반의 JwtAuthentication 또는 AnonymousAuthenticationToken 타입만 지원합니다.");
    }
}
