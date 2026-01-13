package com.chip.board.baselinesync.model;

public record DeltaPageTarget(long userId, String bojHandle, int nextPage, int observedSolvedCount) {}
