package com.chip.board.challenge.application.port;

import com.chip.board.challenge.domain.Challenge;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChallengeLoadPort {
    Optional<Challenge> findActive();
    Optional<Challenge> findFirstOpen(); // ACTIVE/SCHEDULED 중 하나 (정렬/우선순위 포함)
    Optional<Challenge> findLatestUnfinalizedClosed(); // closeFinalized=false, endAt desc
    Optional<Challenge> findById(long id);
    boolean existsAnyOpen(); // ACTIVE or SCHEDULED
    boolean existsOverlappingRange(LocalDateTime startAt, LocalDateTime endAt);
}