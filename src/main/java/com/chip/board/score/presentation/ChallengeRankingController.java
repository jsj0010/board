package com.chip.board.score.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.score.application.service.ChallengeRankingQueryService;
import com.chip.board.score.presentation.dto.response.ChallengeRankingResponse;
import com.chip.board.score.presentation.swagger.ChallengeRankingSwagger;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
public class ChallengeRankingController implements ChallengeRankingSwagger {

    private final ChallengeRankingQueryService challengeRankingQueryService;

    @GetMapping("/{challengeId}/rankings")
    public ResponseEntity<ResponseBody<ChallengeRankingResponse>> getRankings(
            @PathVariable Long challengeId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "8") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(challengeRankingQueryService.getRankingsAllUsers(challengeId, page, size)));
    }
}
