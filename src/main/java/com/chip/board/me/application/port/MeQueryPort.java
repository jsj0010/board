package com.chip.board.me.application.port;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface MeQueryPort {

    List<Row> findByUserIdAndChallengeIdBetween(
            long userId,
            long challengeId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    record Row(
            int problemId,
            String titleKo,
            int level,
            String tierName,
            int points,
            Instant solvedAt
    ) {}
}