package com.chip.board.challenge.repository;

import com.chip.board.challenge.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // [startAt, endAt) 기준 겹침 여부
    @Query("""
        select (count(c) > 0) from Challenge c
        where c.startAt < :endAt and :startAt < c.endAt
    """)
    boolean existsOverlappingRange(@Param("startAt") LocalDateTime startAt,
                                   @Param("endAt") LocalDateTime endAt);
}