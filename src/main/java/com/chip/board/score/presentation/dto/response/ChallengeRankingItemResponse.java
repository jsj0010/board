package com.chip.board.score.presentation.dto.response;

public record ChallengeRankingItemResponse(
        Integer rankNo,      // current_rank_no (없으면 null)
        String name,
        String bojId,
        String department,
        int solvedCount,     // 없으면 0
        long totalPoints,    // 없으면 0
        Integer delta        // rank 둘 중 하나 null이면 null
) {}