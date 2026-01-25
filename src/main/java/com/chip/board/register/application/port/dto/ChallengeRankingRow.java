package com.chip.board.register.application.port.dto;

public record ChallengeRankingRow(
        long userId,
        String name,
        String bojId,
        String department,
        int solvedCount,
        long totalPoints,
        Integer lastRankNo,
        Integer currentRankNo,
        Integer delta
) {
}
