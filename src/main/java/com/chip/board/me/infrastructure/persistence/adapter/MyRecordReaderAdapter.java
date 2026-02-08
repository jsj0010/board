package com.chip.board.me.infrastructure.persistence.adapter;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.me.application.port.MyRecordReader;
import com.chip.board.me.application.service.model.MyChallengeProgress;
import com.chip.board.me.application.service.model.MyRecordSummary;
import com.chip.board.me.application.service.model.MyRecordWeek;
import com.chip.board.me.application.service.model.PagedResult;
import com.chip.board.me.infrastructure.persistence.repository.MyRecordJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MyRecordReaderAdapter implements MyRecordReader {

    private final MyRecordJdbcRepository myRecordJdbcRepository;

    @Override
    public MyRecordSummary loadSummary(long userId) {
        return myRecordJdbcRepository.findSummary(userId);
    }

    @Override
    public PagedResult<MyRecordWeek> loadWeeks(long userId, int page, int size) {
        if (page < 0 || size <= 0) {
            throw new ServiceException(ErrorCode.INVALID_PAGE);
        }

        final int offset;
        try {
            long offsetL = Math.multiplyExact((long) page, (long) size);
            if (offsetL > Integer.MAX_VALUE) throw new ServiceException(ErrorCode.INVALID_PAGE);
            offset = (int) offsetL;
        } catch (ArithmeticException e) {
            throw new ServiceException(ErrorCode.INVALID_PAGE);
        }

        List<MyRecordWeek> rows = myRecordJdbcRepository.findWeeks(userId, size + 1, offset);

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        return new PagedResult<>(rows, page, size, hasNext);
    }

    @Override
    public MyChallengeProgress loadChallengeProgress(long userId, long challengeId) {
        var cur = myRecordJdbcRepository.loadCurrent(challengeId, userId);
        long delta = myRecordJdbcRepository.loadTodayDelta(challengeId, userId);
        return new MyChallengeProgress(cur.currentRank(), cur.currentScore(), delta);
    }
}