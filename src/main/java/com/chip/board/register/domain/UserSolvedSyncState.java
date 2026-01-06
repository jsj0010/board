package com.chip.board.register.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_solved_sync_state",
        indexes = {
                @Index(name = "idx_sync_progress", columnList = "sync_in_progress,user_id"),
                @Index(name = "idx_sync_baseline_ready", columnList = "baseline_ready,user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserSolvedSyncState {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * PK = FK 공유(유저당 1행)
     */
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "baseline_ready", nullable = false)
    @Builder.Default
    private boolean baselineReady = false;

    @Column(name = "baseline_next_page", nullable = false)
    @Builder.Default
    private int baselineNextPage = 1;

    @Column(name = "sync_in_progress", nullable = false)
    @Builder.Default
    private boolean syncInProgress = false;

    @Column(name = "last_solved_count", nullable = false)
    @Builder.Default
    private int lastSolvedCount = 0;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    // 필요하면 상태 변경 메서드들 추가
    public void markBaselineReady() {
        this.baselineReady = true;
        this.baselineNextPage = 1;
    }

    public void startSync() {
        this.syncInProgress = true;
    }

    public void finishSync(int newSolvedCount) {
        this.syncInProgress = false;
        this.lastSolvedCount = newSolvedCount;
        this.baselineNextPage = 1;
        this.lastSyncAt = LocalDateTime.now();
    }
}


