package com.chip.board.baselinesync.model;

import java.sql.Timestamp;

public record BaselineTarget(
        long userId,
        String bojHandle,
        int nextPage
) {}
