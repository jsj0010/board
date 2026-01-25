package com.chip.board.score.domain;

import com.chip.board.register.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "challenge_user_result")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeUserResultEntity {

    @EmbeddedId
    private ChallengeUserResultId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "solved_count", nullable = false)
    private int solvedCount;

    @Column(name = "total_points", nullable = false)
    private long totalPoints;

    @Column(name = "last_rank_no")
    private Integer lastRankNo;

    @Column(name = "current_rank_no")
    private Integer currentRankNo;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ChallengeUserResultEntity of(long challengeId, long userId) {
        ChallengeUserResultEntity challengeUserResultEntity = new ChallengeUserResultEntity();
        challengeUserResultEntity.id = new ChallengeUserResultId(challengeId, userId);
        challengeUserResultEntity.solvedCount = 0;
        challengeUserResultEntity.totalPoints = 0L;
        challengeUserResultEntity.lastRankNo = null;
        challengeUserResultEntity.currentRankNo = null;
        return challengeUserResultEntity;
    }
}