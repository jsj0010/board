package com.chip.board.syncproblem.application.port;

public interface ScoreEventPort {
    void insertScoreEventsForUncredited(long challengeId, long userId);
    void fillCreditedAtFromScoreEvent(long userId);
}
