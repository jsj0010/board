package com.chip.board.challenge.service;

import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeStatusService {

    private final ChallengeRepository challengeRepository;
    private final Clock clock;

    @Transactional
    public void updateChallengeStatus() {
        LocalDateTime now = LocalDateTime.now(clock);

        challengeRepository
                .findFirstByStatusIn(List.of(ChallengeStatus.SCHEDULED, ChallengeStatus.ACTIVE))
                .ifPresent(challenge -> updateOne(challenge, now));
    }

    private void updateOne(Challenge challenge, LocalDateTime now) {
        if (shouldClose(challenge, now)) {
            challenge.close();
            return;
        }

        if (shouldActivate(challenge, now)) {
            challenge.activate(now);
        }
    }

    private boolean shouldClose(Challenge challenge, LocalDateTime now) {
        // 상태가 SCHEDULED/ACTIVE인 것만 조회하므로 CLOSED 체크는 사실상 불필요하지만 방어적으로 둠
        return !now.isBefore(challenge.getEndAt()) && challenge.getStatus() != ChallengeStatus.CLOSED;
    }

    private boolean shouldActivate(Challenge challenge, LocalDateTime now) {
        return challenge.getStatus() == ChallengeStatus.SCHEDULED
                && !now.isBefore(challenge.getStartAt())
                && now.isBefore(challenge.getEndAt());
    }
}
