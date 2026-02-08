package com.chip.board.me.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.me.presentation.dto.response.DailySolvedProblemsResponse;
import com.chip.board.me.presentation.dto.response.MyChallengeSummaryResponse;
import com.chip.board.me.presentation.dto.response.MyRecordSummaryResponse;
import com.chip.board.me.presentation.dto.response.MyRecordWeeksResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Me Record", description = "내 기록/챌린지 관련 API")
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

    @Operation(
            summary = "내 기록 요약 조회",
            description = "로그인한 사용자의 기록(요약) 정보를 조회합니다."
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = MyRecordSummaryResponse.class,
                    description = "내 기록 요약 조회 성공"
            ),
            errors = {
                    // 필요 시 ErrorCode 추가
            }
    )
    @GetMapping("/summary")
    ResponseEntity<ResponseBody<MyRecordSummaryResponse>> summary(
            @Parameter(hidden = true)
            Long userId
    );

    @Operation(
            summary = "주차별 기록 요약 목록 조회",
            description = "로그인한 사용자의 주차별 기록 요약을 페이지네이션으로 조회합니다."
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = MyRecordWeeksResponse.class,
                    description = "주차별 기록 요약 목록 조회 성공"
            ),
            errors = {
                    // 필요 시 ErrorCode 추가
            }
    )
    @GetMapping("/weeks")
    ResponseEntity<ResponseBody<MyRecordWeeksResponse>> weeks(
            @Parameter(hidden = true)
            Long userId,

            @Parameter(
                    name = "page",
                    description = "페이지(0부터)",
                    required = false,
                    example = "0"
            )
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(
                    name = "size",
                    description = "페이지 크기(1~100)",
                    required = false,
                    example = "10"
            )
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    );

    @Operation(
            summary = "내 챌린지 진행 요약 조회",
            description = """
                    로그인한 사용자의 특정 챌린지 진행 요약을 조회합니다.
                    - currentRank: 현재 순위
                    - currentScore: 현재 점수
                    - scoreDelta: 오늘 00:00~현재 증가 점수(서버 집계 기준)
                    - goalScore: 사용자의 목표 점수(user.goal_points)
                    - achievementRate: 달성률(0~1)
                    """
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = MyChallengeSummaryResponse.class,
                    description = "내 챌린지 진행 요약 조회 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.CHALLENGE_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND)
            }
    )
    @GetMapping("/{challengeId}/progress-summary")
    ResponseEntity<ResponseBody<MyChallengeSummaryResponse>> myChallengeProgressSummary(
            @Parameter(hidden = true)
            Long userId,

            @Parameter(name = "challengeId", description = "챌린지 ID", required = true, example = "1")
            @PathVariable("challengeId") Long challengeId
    );
}
