package com.chip.board.challenge.application.scheduler;

import com.chip.board.challenge.application.service.ChallengeStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeStatusScheduler {

    private final ChallengeStatusService challengeStatusService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void updateChallengeStatus() {
        challengeStatusService.updateChallengeStatus();
    }
}