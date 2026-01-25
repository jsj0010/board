package com.chip.board.score.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ChallengeUserResultId implements Serializable {

    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public ChallengeUserResultId(long challengeId, long userId) {
        this.challengeId = challengeId;
        this.userId = userId;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public Long getUserId() {
        return userId;
    }
}