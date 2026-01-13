package com.chip.board.challenge.batch;

import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.time.Clock;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeStatusScheduler {

    private final ChallengeRepository challengeRepository;
    private final Clock clock;

    @Scheduled(cron = "0 00 00 * * *", zone = "Asia/Seoul")
    @Transactional
    public void updateChallengeStatus() {
        LocalDateTime now = LocalDateTime.now(clock);

        challengeRepository
                .findFirstByStatusIn(List.of(ChallengeStatus.SCHEDULED, ChallengeStatus.ACTIVE))
                .ifPresent(challenge -> {
                    if (!now.isBefore(challenge.getEndAt())) {
                        if (challenge.getStatus() != ChallengeStatus.CLOSED) {
                            challenge.close();
                            log.info("[ChallengeScheduler] closed. id={}, endAt={}", challenge.getChallengeId(), challenge.getEndAt());
                        }
                    } else if (challenge.getStatus() == ChallengeStatus.SCHEDULED
                            && !now.isBefore(challenge.getStartAt())
                            && now.isBefore(challenge.getEndAt())) {
                        challenge.activate(now);
                        log.info("[ChallengeScheduler] activated. id={}, startAt={}, endAt={}",
                                challenge.getChallengeId(), challenge.getStartAt(), challenge.getEndAt());
                    }
                });
    }
}