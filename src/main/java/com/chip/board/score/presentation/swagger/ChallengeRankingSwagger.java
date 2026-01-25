package com.chip.board.score.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.score.presentation.dto.response.ChallengeRankingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Challenge Ranking", description = "챌린지 랭킹 조회 API")
public interface ChallengeRankingSwagger {

    @Operation(
            summary = "챌린지 랭킹 조회",
            description = "challengeId에 대한 전체 유저 랭킹을 페이지네이션으로 조회합니다."
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = ChallengeRankingResponse.class,
                    description = "챌린지 랭킹 조회 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.CHALLENGE_NOT_FOUND)
            }
    )
    ResponseEntity<ResponseBody<ChallengeRankingResponse>> getRankings(
            @Parameter(name = "challengeId", description = "챌린지 ID", required = true, example = "1")
            @PathVariable("challengeId") Long challengeId,

            @Parameter(name = "page", description = "페이지(0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(name = "size", description = "페이지 크기(1~100)", example = "8")
            @RequestParam(defaultValue = "8") @Min(1) @Max(100) int size
    );
}
