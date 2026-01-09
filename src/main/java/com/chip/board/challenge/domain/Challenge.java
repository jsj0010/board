package com.chip.board.challenge.domain;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.global.config.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "challenge",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_challenge_range", columnNames = {"start_at", "end_at"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChallengeStatus status = ChallengeStatus.SCHEDULED;

    public Challenge(String title, LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ServiceException(ErrorCode.CHALLENGE_RANGE_INVALID);
        }
        this.title = title;
        this.startAt = startDate;
        this.endAt = endDate;
        this.status = ChallengeStatus.SCHEDULED;
    }

    public void activate(LocalDateTime now) {
        if (status != ChallengeStatus.SCHEDULED) {
            throw new ServiceException(ErrorCode.CHALLENGE_STATUS_INVALID_TRANSITION);
        }
        if (now.isBefore(startAt) || !now.isBefore(endAt)) {
            throw new ServiceException(ErrorCode.CHALLENGE_NOT_IN_ACTIVE_RANGE);
        }
        this.status = ChallengeStatus.ACTIVE;
    }

    public void close() {
        this.status = ChallengeStatus.CLOSED;
    }
}