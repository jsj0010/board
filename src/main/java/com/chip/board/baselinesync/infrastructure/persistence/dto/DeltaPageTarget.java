package com.chip.board.baselinesync.infrastructure.persistence.dto;

public record DeltaPageTarget(long userId, String bojHandle, int nextPage, int observedSolvedCount) {}
