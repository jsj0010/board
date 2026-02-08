package com.chip.board.me.presentation.dto.response;

public record MyChallengeSummaryResponse(
        Integer currentRank,
        Long currentScore,
        Long scoreDelta,
        Long goalScore,
        Double achievementRate
) {
    public static MyChallengeSummaryResponse of(Integer rank, long score, long delta, long goal) {
        double rate = (goal > 0) ? (score / (double) goal) : 0.0;
        if (rate > 1.0) rate = 1.0;
        if (rate < 0.0) rate = 0.0;
        return new MyChallengeSummaryResponse(rank, score, delta, goal, rate);
    }
}