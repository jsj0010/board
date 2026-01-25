package com.chip.board.challenge.application.port.dto;

import java.time.LocalDateTime;

public record ChallengeRankingAggregate(
        long totalUserCount,
        long participantsCount,
        long totalSolvedCount,
        LocalDateTime lastUpdatedAt
) {
}
