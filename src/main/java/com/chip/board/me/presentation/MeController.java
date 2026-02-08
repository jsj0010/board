package com.chip.board.me.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.global.jwt.annotation.CurrentUserId;
import com.chip.board.me.application.service.DailySolvedProblemQueryService;
import com.chip.board.me.application.service.MyRecordQueryService;
import com.chip.board.me.presentation.dto.response.DailySolvedProblemsResponse;
import com.chip.board.me.presentation.dto.response.MyChallengeSummaryResponse;
import com.chip.board.me.presentation.dto.response.MyRecordSummaryResponse;
import com.chip.board.me.presentation.dto.response.MyRecordWeeksResponse;
import com.chip.board.me.presentation.swagger.MeSwagger;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/records")
public class MeController implements MeSwagger {

    private final DailySolvedProblemQueryService dailySolvedProblemQueryService;
    private final MyRecordQueryService myRecordQueryService;

    @GetMapping("/{challengeId}/solved-problems")
    public ResponseEntity<ResponseBody<DailySolvedProblemsResponse>> getDailySolvedProblems(
            @CurrentUserId Long userId,
            @PathVariable Long challengeId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailySolvedProblemsResponse res = dailySolvedProblemQueryService.getDailySolvedProblems(userId, challengeId, date);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(res));
    }

    @GetMapping("/summary")
    public ResponseEntity<ResponseBody<MyRecordSummaryResponse>> summary(
            @CurrentUserId Long userId
    ) {
        MyRecordSummaryResponse res = myRecordQueryService.getMyRecordSummary(userId);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(res));
    }

    @GetMapping("/weeks")
    public ResponseEntity<ResponseBody<MyRecordWeeksResponse>> weeks(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        MyRecordWeeksResponse res = myRecordQueryService.getWeeksSummary(userId, page, size);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(res));
    }

    @GetMapping("/{challengeId}/summary")
    public ResponseEntity<ResponseBody<MyChallengeSummaryResponse>> myChallengeProgressSummary(
            @PathVariable Long challengeId,
            @CurrentUserId Long userId
    ) {
        MyChallengeSummaryResponse data = myRecordQueryService.getMyChallengeProgressSummary(challengeId, userId);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(data));
    }
}