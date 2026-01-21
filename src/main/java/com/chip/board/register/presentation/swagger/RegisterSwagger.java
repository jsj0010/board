package com.chip.board.register.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.register.presentation.dto.request.MailRequest;
import com.chip.board.register.presentation.dto.request.MailVerifyRequest;
import com.chip.board.register.presentation.dto.request.UserRegisterRequest;
import com.chip.board.register.presentation.dto.request.ValidateBaekjoonHandleRequest;
import com.chip.board.register.presentation.dto.response.ValidateBaekjoonHandleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RegisterSwagger {

    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String[].class,
                    description = "학과 목록 조회 성공"
            )
    )
    ResponseEntity<ResponseBody<List<String>>> showRegisterForm();

    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.CREATED,
                    response = String.class,
                    description = "회원가입 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.INVALID_DEPARTMENT),
                    @SwaggerApiFailedResponse(ErrorCode.EMAIL_NOT_VERIFIED),
                    @SwaggerApiFailedResponse(ErrorCode.SYNC_STATE_ALREADY_EXISTS)
            }
    )
    ResponseEntity<ResponseBody<String>> register(UserRegisterRequest req);

    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String.class,
                    description = "이메일 전송 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.USER_ALREADY_EXIST),
            }
    )
    ResponseEntity<ResponseBody<String>> mailSend(MailRequest req);

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
    ResponseEntity<ResponseBody<String>> verifyMail(MailVerifyRequest req);

    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = ValidateBaekjoonHandleResponse.class,
                    description = "백준 핸들 검증 결과"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.DUPLICATE_BOJ_ID),
                    @SwaggerApiFailedResponse(ErrorCode.SOLVEDAC_COOLDOWN_ACTIVE),
                    @SwaggerApiFailedResponse(ErrorCode.BAEKJOON_HANDLE_INVALID)
            }
    )
    ResponseEntity<ResponseBody<ValidateBaekjoonHandleResponse>> validate(ValidateBaekjoonHandleRequest req);
}
