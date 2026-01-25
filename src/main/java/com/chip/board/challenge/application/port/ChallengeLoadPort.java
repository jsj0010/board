package com.chip.board.challenge.application.port;

import com.chip.board.challenge.application.port.dto.ChallengeRankingAggregate;
import com.chip.board.challenge.domain.Challenge;
import com.chip.board.syncproblem.application.port.dto.ChallengeSyncSnapshot;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChallengeLoadPort {
    Optional<Challenge> findActive();
    Optional<Challenge> findFirstOpen(); // ACTIVE/SCHEDULED 중 하나 (정렬/우선순위 포함)
    Optional<Challenge> findById(Long id);
    boolean existsAnyOpen(); // ACTIVE or SCHEDULED
    boolean existsOverlappingRange(LocalDateTime startAt, LocalDateTime endAt);
    boolean existsById(Long id);

    boolean existsActive();
    boolean existsClosedUnfinalized();
    Optional<ChallengeSyncSnapshot> findCurrentSyncTarget();

    ChallengeRankingAggregate getRankingAggregate(Long challengeId);
}