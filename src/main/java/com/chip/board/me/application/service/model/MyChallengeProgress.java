package com.chip.board.me.application.service.model;

public record MyChallengeProgress(
        Integer currentRank,
        long currentScore,
        long scoreDelta
) {}
