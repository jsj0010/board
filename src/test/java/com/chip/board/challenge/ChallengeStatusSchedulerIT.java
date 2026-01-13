package com.chip.board.challenge;

import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.repository.ChallengeRepository;
import com.chip.board.challenge.batch.ChallengeStatusScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ChallengeStatusSchedulerIT {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @TestConfiguration
    static class ClockTestConfig {
        @Bean
        public MutableClock mutableClock() {
            return new MutableClock(Instant.EPOCH, KST);
        }

        @Bean
        @Primary
        public Clock testClock(MutableClock mutableClock) {
            return mutableClock;
        }
    }

    @Autowired private ChallengeRepository challengeRepository;
    @Autowired private ChallengeStatusScheduler scheduler;
    @Autowired private MutableClock clock;

    @BeforeEach
    void setUp() {
        challengeRepository.deleteAll();
    }

    @Test
    void startAt_되면_SCHEDULED_to_ACTIVE() {
        LocalDateTime startAt = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime endAtInDb = LocalDateTime.of(2026, 1, 12, 0, 0);

        Challenge saved = challengeRepository.save(
                new Challenge("2026W02", startAt, endAtInDb.minusDays(1))
        );

        // start 직전
        clock.setInstant(startAt.minusSeconds(1).atZone(KST).toInstant());
        scheduler.updateChallengeStatus();

        Challenge after1 = challengeRepository.findById(saved.getChallengeId()).orElseThrow();
        assertThat(after1.getStatus()).isEqualTo(ChallengeStatus.SCHEDULED);

        // start 시각
        clock.setInstant(startAt.atZone(KST).toInstant());
        scheduler.updateChallengeStatus();

        Challenge after2 = challengeRepository.findById(saved.getChallengeId()).orElseThrow();
        assertThat(after2.getStatus()).isEqualTo(ChallengeStatus.ACTIVE);
    }

    @Test
    void endAt_되면_CLOSED() {
        LocalDateTime startAt = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime endAtInDb = LocalDateTime.of(2026, 1, 12, 0, 0);

        Challenge saved = challengeRepository.save(
                new Challenge("2026W02", startAt, endAtInDb.minusDays(1))
        );

        // ACTIVE로 전환
        clock.setInstant(startAt.atZone(KST).toInstant());
        scheduler.updateChallengeStatus();

        // end 시각 -> CLOSED
        clock.setInstant(endAtInDb.atZone(KST).toInstant());
        scheduler.updateChallengeStatus();

        Challenge after = challengeRepository.findById(saved.getChallengeId()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(ChallengeStatus.CLOSED);
    }
}
