package com.chip.board.syncproblem.application.scheduler;

import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.infrastructure.persistence.ChallengeRepository;
import com.chip.board.syncproblem.application.service.ChallengeObserveEnqueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChallengeObserveEnqueueScheduler {

    private final ChallengeRepository challengeRepository;
    private final ChallengeObserveEnqueueService challengeObserveEnqueueService;
    private final Clock clock;

    // 매일 00:00:00에 1번
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void enqueue() {

        boolean shouldRun =
                challengeRepository.existsByStatus(ChallengeStatus.ACTIVE)
                        || challengeRepository.existsByStatusAndCloseFinalized(ChallengeStatus.CLOSED, false);

        if (!shouldRun) return;

        LocalDateTime windowStart = LocalDate.now(clock).atTime(0, 0, 0);
        challengeObserveEnqueueService.enqueueObserveWindow(windowStart);
    }
}
