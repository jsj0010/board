package com.chip.board.challenge.application.scheduler;

import com.chip.board.challenge.application.port.ChallengeLoadPort;
import com.chip.board.challenge.application.service.ChallengeStatusService;
import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.application.port.ChallengeSyncIndexPort;
import com.chip.board.syncproblem.application.port.dto.ChallengeSyncSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeStatusScheduler {

    private final ChallengeStatusService challengeStatusService;
    private final ChallengeLoadPort challengeLoadPort;
    private final ChallengeSyncIndexPort challengeSyncIndexPort;

    @Scheduled(cron = "0 51 15 * * *", zone = "Asia/Seoul")
    public void updateChallengeStatus() {

        challengeStatusService.updateChallengeStatus();

        boolean shouldRun = shouldRun();
        if (!shouldRun) return;

        Optional<ChallengeSyncSnapshot> challengeSyncSnapshot = challengeLoadPort.findCurrentSyncTarget();

        if (challengeSyncSnapshot.isEmpty()) {
            challengeSyncIndexPort.delete();
            return;
        }

        ChallengeSyncSnapshot snap = challengeSyncSnapshot.get();
        // CLOSED & closeFinalized=true면 더 이상 처리 대상이 아니므로 삭제
        if (snap.status() == ChallengeStatus.CLOSED && snap.closeFinalized()) {
            challengeSyncIndexPort.delete();
            return;
        }

        challengeSyncIndexPort.save(snap);

    }

    private boolean shouldRun() {
        return challengeLoadPort.existsActive()
                || challengeLoadPort.existsClosedUnfinalized();
    }
}