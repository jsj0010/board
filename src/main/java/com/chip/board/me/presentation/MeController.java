package com.chip.board.me.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.global.jwt.annotation.CurrentUserId;
import com.chip.board.me.application.service.DailySolvedProblemQueryService;
import com.chip.board.me.presentation.dto.response.DailySolvedProblemsResponse;
import com.chip.board.me.presentation.swagger.MeSwagger;
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
@RequestMapping("/api/me/challenges")
public class MeController implements MeSwagger {

    private final DailySolvedProblemQueryService dailySolvedProblemQueryService;

    @GetMapping("/{challengeId}/solved-problems")
    public ResponseEntity<ResponseBody<DailySolvedProblemsResponse>> getDailySolvedProblems(
            @CurrentUserId Long userId,
            @PathVariable Long challengeId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailySolvedProblemsResponse res = dailySolvedProblemQueryService.getDailySolvedProblems(userId, challengeId, date);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(res));
    }
}