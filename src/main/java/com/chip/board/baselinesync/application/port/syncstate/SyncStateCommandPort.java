package com.chip.board.baselinesync.application.port.syncstate;

public interface SyncStateCommandPort {
    void updateObserved(long userId, int observedSolvedCount);
    void advancePage(long userId);
    void finishDelta(long userId, int observedSolvedCount);
}

