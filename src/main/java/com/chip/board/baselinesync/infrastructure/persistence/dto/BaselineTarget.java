package com.chip.board.baselinesync.infrastructure.persistence.dto;

public record BaselineTarget(
        long userId,
        String bojHandle,
        int nextPage
) {}
