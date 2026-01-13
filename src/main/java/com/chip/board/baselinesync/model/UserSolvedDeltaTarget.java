package com.chip.board.baselinesync.model;

public record UserSolvedDeltaTarget(
        long userId,
        String bojHandle,
        int lastSolvedCount,
        int observedSolvedCount
) {}
