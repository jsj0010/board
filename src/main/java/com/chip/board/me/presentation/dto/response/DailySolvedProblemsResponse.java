package com.chip.board.me.presentation.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DailySolvedProblemsResponse(
        LocalDate date,
        int count,
        List<Item> items
) {
    public record Item(
            int problemId,
            String titleKo,
            int level,
            String tierName,
            int points,
            Instant solvedAt
    ) {}
}
