package com.chip.board.oauth.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.oauth.presentation.dto.request.PasswordMailRequest;
import com.chip.board.oauth.presentation.dto.request.PasswordResetRequest;
import com.chip.board.oauth.presentation.dto.request.PasswordVerifyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag(name = "Password", description = "비밀번호 재설정(메일 인증/검증/변경) API")
public interface PasswordSwagger {

    @Operation(summary = "비밀번호 재설정 메일 발송", description = "입력한 이메일(username)로 비밀번호 재설정 인증코드를 발송합니다.")
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String.class,
                    description = "이메일 전송 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND),
            }
    )
    ResponseEntity<ResponseBody<String>> mailSend(PasswordMailRequest req);

    @Operation(summary = "비밀번호 재설정 메일 인증코드 검증", description = "비밀번호 재설정 인증코드를 검증합니다.")
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String.class,
                    description = "인증 완료"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.EXPIRED_EMAIL_CODE),
                    @SwaggerApiFailedResponse(ErrorCode.INVALID_EMAIL_CODE)
            }
    )
    ResponseEntity<ResponseBody<String>> verifyMail(PasswordVerifyRequest req);

    @Operation(summary = "비밀번호 재설정", description = "인증 완료된 사용자에 대해 새 비밀번호로 변경합니다.")
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String.class,
                    description = "비밀번호 변경 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.EMAIL_NOT_VERIFIED),
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND)
            }
    )
    ResponseEntity<ResponseBody<String>> resetPassword(PasswordResetRequest req);
}
