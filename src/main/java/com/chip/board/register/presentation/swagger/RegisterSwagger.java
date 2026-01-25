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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Register", description = "회원가입/이메일 인증 API")
public interface RegisterSwagger {

    @Operation(summary = "학과 목록 조회", description = "회원가입 폼에 필요한 학과 목록을 조회합니다.")
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String[].class, // data가 배열로 보이도록
                    description = "학과 목록 조회 성공"
            )
    )
    ResponseEntity<ResponseBody<List<String>>> showRegisterForm();

    @Operation(summary = "회원가입", description = "이메일 인증 완료 후 회원가입을 진행합니다.")
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.CREATED,
                    response = String.class, // data: "string" (메시지/토큰 등 문자열 반환이면 정상)
                    description = "회원가입 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.INVALID_DEPARTMENT),
                    @SwaggerApiFailedResponse(ErrorCode.EMAIL_NOT_VERIFIED),
                    @SwaggerApiFailedResponse(ErrorCode.SYNC_STATE_ALREADY_EXISTS)
            }
    )
    ResponseEntity<ResponseBody<String>> register(
            @Parameter(description = "회원가입 요청", required = true)
            UserRegisterRequest req
    );

    @Operation(summary = "인증 메일 전송", description = "입력한 이메일로 인증 코드를 전송합니다.")
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
    ResponseEntity<ResponseBody<String>> mailSend(
            @Parameter(description = "메일 전송 요청", required = true)
            MailRequest req
    );

    @Operation(summary = "이메일 인증", description = "이메일로 받은 인증 코드를 검증합니다.")
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
    ResponseEntity<ResponseBody<String>> verifyMail(
            @Parameter(description = "메일 인증 요청", required = true)
            MailVerifyRequest req
    );

    @Operation(summary = "백준 핸들 검증", description = "백준 아이디 유효성/중복/쿨다운을 검증합니다.")
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
    ResponseEntity<ResponseBody<ValidateBaekjoonHandleResponse>> validate(
            @Parameter(description = "백준 핸들 검증 요청", required = true)
            ValidateBaekjoonHandleRequest req
    );
}
