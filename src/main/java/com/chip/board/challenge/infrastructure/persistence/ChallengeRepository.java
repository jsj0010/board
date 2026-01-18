package com.chip.board.challenge.infrastructure.persistence;

import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // [startAt, endAt) 기준 겹침 여부
    @Query("""
        select (count(c) > 0) from Challenge c
        where c.startAt < :endAt and :startAt < c.endAt
    """)
    boolean existsOverlappingRange(@Param("startAt") LocalDateTime startAt,
                                   @Param("endAt") LocalDateTime endAt);

    boolean existsByStatusIn(Collection<ChallengeStatus> statuses);

    Optional<Challenge> findTopByStatusAndCloseFinalizedFalseOrderByEndAtDesc(ChallengeStatus status);

    Optional<Challenge> findFirstByStatusIn(Collection<ChallengeStatus> statuses);

    Optional<Challenge> findFirstByStatus(ChallengeStatus status);

    boolean existsByStatus(ChallengeStatus status);

    boolean existsByStatusAndCloseFinalized(ChallengeStatus status, boolean closeFinalized);
}