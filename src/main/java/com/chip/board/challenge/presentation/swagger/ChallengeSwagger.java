package com.chip.board.challenge.presentation.swagger;

import com.chip.board.challenge.presentation.dto.request.ChallengeCreateRequest;
import com.chip.board.challenge.presentation.dto.response.ChallengeInfoResponse;
import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag(name = "Challenge", description = "챌린지 생성/조회 API")
public interface ChallengeSwagger {

    @Operation(summary = "챌린지 홀드(생성)", description = "관리자 권한으로 챌린지를 홀드(생성)합니다.")
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = ChallengeInfoResponse.class,
                    description = "챌린지 홀드 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.CHALLENGE_RANGE_INVALID),
                    @SwaggerApiFailedResponse(ErrorCode.CHALLENGE_ALREADY_EXISTS),
                    @SwaggerApiFailedResponse(ErrorCode.CHALLENGE_RANGE_OVERLAPS)
            }
    )
    ResponseEntity<ResponseBody<ChallengeInfoResponse>> hold(ChallengeCreateRequest request);

    @Operation(summary = "챌린지 정보 조회", description = "challengeId로 챌린지 정보를 조회합니다.")
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = ChallengeInfoResponse.class,
                    description = "챌린지 조회 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.CHALLENGE_NOT_FOUND)
            }
    )
    ResponseEntity<ResponseBody<ChallengeInfoResponse>> info(@Parameter(name = "challengeId", description = "챌린지 ID", required = true, example = "1") Long challengeId);
}

