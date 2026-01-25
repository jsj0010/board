package com.chip.board.me.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.me.presentation.dto.response.DailySolvedProblemsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Me Challenge", description = "내 챌린지 관련 API")
public interface MeSwagger {

    @Operation(
            summary = "일자별 풀이 문제 조회",
            description = """
                    로그인한 사용자가 특정 챌린지에서 특정 날짜에 푼 문제 목록을 조회합니다.
                    - date는 ISO_DATE(yyyy-MM-dd) 형식입니다.
                    """
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = DailySolvedProblemsResponse.class,
                    description = "일자별 풀이 문제 조회 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.CHALLENGE_NOT_FOUND)
            }
    )
    @GetMapping("/{challengeId}/solved-problems")
    ResponseEntity<ResponseBody<DailySolvedProblemsResponse>> getDailySolvedProblems(
            @Parameter(hidden = true)
            Long userId,

            @Parameter(name = "challengeId", description = "챌린지 ID", required = true, example = "1")
            @PathVariable("challengeId") Long challengeId,

            @Parameter(
                    name = "date",
                    description = "조회 날짜(yyyy-MM-dd)",
                    required = true,
                    example = "2026-01-25"
            )
            @RequestParam("date") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}
