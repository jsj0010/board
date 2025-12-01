package com.chip.board.global.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;


@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    UNEXPECTED_SERVER_ERROR(INTERNAL_SERVER_ERROR,"COM_001","예상치 못한 서버 오류가 발생했습니다."),
    BINDING_ERROR(BAD_REQUEST,"COM_002","요청 데이터 변환 과정에서 오류가 발생했습니다."),
    ESSENTIAL_FIELD_MISSING_ERROR(NO_CONTENT , "COM_003","필수 필드를 누락했습니다."),
    INVALID_ENDPOINT(NOT_FOUND, "COM_004", "잘못된 API URI로 요청했습니다."),
    INVALID_HTTP_METHOD(METHOD_NOT_ALLOWED, "COM_005","잘못된 HTTP 메서드로 요청했습니다."),
    // Authentication
    NEED_AUTHORIZED(UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    ACCESS_DENIED(FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    JWT_EXPIRED(UNAUTHORIZED, "AUTH_003", "인증 정보가 만료되었습니다."),
    JWT_INVALID(UNAUTHORIZED, "AUTH_004", "인증 정보가 잘못되었습니다."),
    JWT_NOT_EXIST(UNAUTHORIZED, "AUTH_005", "인증 정보가 존재하지 않습니다."),

    // User
    USER_NOT_FOUND(NOT_FOUND, "USER_001", "해당 사용자를 찾을 수 없습니다.")
    ;
    private final HttpStatus status;
    private final String code;
    private final String message;
}
