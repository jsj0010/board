package com.chip.board.baselinesync.application.port;

import java.util.Optional;

public interface DeltaJobQueuePort {
    Optional<Long> popDueUserId(long nowMs);
    void scheduleAt(long userId, long dueAtMs);
}