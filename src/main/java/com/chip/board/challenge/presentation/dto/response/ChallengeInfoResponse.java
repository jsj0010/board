package com.chip.board.challenge.presentation.dto.response;

import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;

import java.time.LocalDateTime;

public record ChallengeInfoResponse(
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt,
        ChallengeStatus status
) {
    public static ChallengeInfoResponse from(Challenge c) {
        return new ChallengeInfoResponse(
                c.getTitle(),
                c.getStartAt(),
                c.getEndAt(),
                c.getStatus()
        );
    }
}