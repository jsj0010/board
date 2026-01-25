package com.chip.board.challenge.presentation.dto.response;

import com.chip.board.challenge.domain.ChallengeStatus;

import java.time.LocalDateTime;

public record ChallengeDetailInfoResponse(
        // info 엔드포인트가 주던 정보
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt,
        ChallengeStatus status,

        // ranking summary에서만 추가로 내려줄 정보
        long totalUserCount,
        long participantsCount,
        long totalSolvedCount,
        LocalDateTime lastUpdatedAt
) {
}