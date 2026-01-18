package com.chip.board.baselinesync.application.port.syncstate;

import com.chip.board.baselinesync.infrastructure.persistence.dto.DeltaPageTarget;
import com.chip.board.baselinesync.infrastructure.persistence.dto.SyncTarget;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SyncStateQueryPort {
    List<Long> findObserveUserIds(LocalDateTime windowStart);
    Optional<SyncTarget> findObserveTarget(long userId, LocalDateTime windowStart);
    Optional<DeltaPageTarget> findDeltaTarget(long userId, LocalDateTime windowStart);
    boolean existsObservePending(LocalDateTime windowStart);
    boolean existsDeltaPending(LocalDateTime windowStart);
    Optional<Long> pickOneForScoring(); // FOR UPDATE SKIP LOCKED 내부 구현
    boolean existsAnyScoreable();
}