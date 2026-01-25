package com.chip.board.score.presentation.dto.response;

import java.time.Instant;
import java.util.List;

public record ChallengeRankingResponse(
        long challengeId,
        Instant generatedAt,
        int page,
        int size,
        long totalElements,
        boolean hasNext,
        int nextPage, // 없으면 -1
        List<ChallengeRankingItemResponse> items
) {}