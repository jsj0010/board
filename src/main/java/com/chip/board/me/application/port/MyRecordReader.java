package com.chip.board.me.application.port;

import com.chip.board.me.application.service.model.MyChallengeProgress;
import com.chip.board.me.application.service.model.MyRecordSummary;
import com.chip.board.me.application.service.model.MyRecordWeek;
import com.chip.board.me.application.service.model.PagedResult;

public interface MyRecordReader {
    MyRecordSummary loadSummary(long userId);
    PagedResult<MyRecordWeek> loadWeeks(long userId, int page, int size);
    MyChallengeProgress loadChallengeProgress(long userId, long challengeId);
}