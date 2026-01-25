package com.chip.board.syncproblem.application.scheduler;

import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.application.port.ChallengeSyncIndexPort;
import com.chip.board.syncproblem.application.port.dto.ChallengeSyncSnapshot;
import com.chip.board.syncproblem.application.service.ChallengeObserveEnqueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChallengeObserveEnqueueScheduler {

    private final ChallengeSyncIndexPort syncIndexPort;

    private final ChallengeObserveEnqueueService challengeObserveEnqueueService;
    private final Clock clock;

    @Scheduled(cron = "0 0 19 * * *", zone = "Asia/Seoul")
    public void enqueue() {

        Optional<ChallengeSyncSnapshot> snapOpt = syncIndexPort.load();
        if (snapOpt.isEmpty()) return;

        ChallengeSyncSnapshot snap = snapOpt.get();
        if (snap.status() == ChallengeStatus.CLOSED && snap.closeFinalized()) return;

        LocalDateTime windowStart = LocalDate.now(clock).atTime(19, 0, 0);
        challengeObserveEnqueueService.enqueueObserveWindow(windowStart);
    }


}