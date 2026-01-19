package com.chip.board.syncproblem.application.port.dto;

import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;

public record ChallengeSyncSnapshot(
        long challengeId,
        ChallengeStatus status,
        boolean prepareFinalized,
        boolean closeFinalized
) {
    public static ChallengeSyncSnapshot from(Challenge c) {
        return new ChallengeSyncSnapshot(
                c.getChallengeId(),
                c.getStatus(),
                c.isPrepareFinalized(),
                c.isCloseFinalized()
        );
    }
}
