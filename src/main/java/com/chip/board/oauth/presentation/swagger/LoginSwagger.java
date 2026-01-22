package com.chip.board.oauth.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.global.jwt.token.access.AccessTokenData;
import com.chip.board.oauth.presentation.dto.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "로그인/토큰갱신/로그아웃 API")
public interface LoginSwagger {

    @Operation(summary = "로그인", description = "아이디/비밀번호로 로그인하고 AccessToken을 발급합니다.")
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = AccessTokenData.class,
                    description = "로그인 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.INVALID_LOGIN),
            }
    )
    ResponseEntity<ResponseBody<AccessTokenData>> login(LoginRequest request);

    @Operation(summary = "토큰 갱신", description = "refresh_token 쿠키로 AccessToken을 갱신합니다.")
    @Parameter(
            name = "refresh_token",
            description = "Refresh Token (Cookie)",
            required = false,
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.COOKIE
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = AccessTokenData.class,
                    description = "토큰 갱신 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.REFRESH_TOKEN_INVALID),
            }
    )
    ResponseEntity<ResponseBody<AccessTokenData>> refresh(String refreshToken);

    @Operation(summary = "로그아웃", description = "refresh_token 쿠키를 무효화(로그아웃)합니다.")
    @Parameter(
            name = "refresh_token",
            description = "Refresh Token (Cookie)",
            required = false,
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.COOKIE
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = Void.class,
                    description = "로그아웃 성공"
            ),
            errors = {
            }
    )
    ResponseEntity<ResponseBody<Void>> logout(String refreshToken);
}

