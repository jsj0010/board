package com.chip.board.me.application.service;

import com.chip.board.challenge.application.port.ChallengeLoadPort;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.me.application.port.MyRecordReader;
import com.chip.board.me.application.service.model.MyChallengeProgress;
import com.chip.board.me.application.service.model.MyRecordSummary;
import com.chip.board.me.application.service.model.MyRecordWeek;
import com.chip.board.me.application.service.model.PagedResult;
import com.chip.board.me.presentation.dto.response.MyChallengeSummaryResponse;
import com.chip.board.me.presentation.dto.response.MyRecordSummaryResponse;
import com.chip.board.me.presentation.dto.response.MyRecordWeeksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyRecordQueryService {

    private final MyRecordReader myRecordReader;
    private final UserGoalService userGoalService;
    private final ChallengeLoadPort challengeLoadPort;

    @Transactional(readOnly = true)
    public MyRecordSummaryResponse getMyRecordSummary(long userId) {
        MyRecordSummary myRecordSummary = myRecordReader.loadSummary(userId);
        return new MyRecordSummaryResponse(
                myRecordSummary.maxPoints(),
                myRecordSummary.totalSolvedCount()
        );
    }

    @Transactional(readOnly = true)
    public MyRecordWeeksResponse getWeeksSummary(long userId, int page, int size) {
        PagedResult<MyRecordWeek> r = myRecordReader.loadWeeks(userId, page, size);
        return new MyRecordWeeksResponse(
                r.items().stream()
                        .map(w -> new MyRecordWeeksResponse.MyRecordWeekItem(
                                w.week(), w.solvedCount(), w.score(), w.rank()
                        ))
                        .toList(),
                r.page(),
                r.size(),
                r.hasNext()
        );
    }

    @Transactional(readOnly = true)
    public MyChallengeSummaryResponse getMyChallengeProgressSummary(long challengeId, long userId) {
        if (!challengeLoadPort.existsById(challengeId)) {
            throw new ServiceException(ErrorCode.CHALLENGE_NOT_FOUND);
        }

        MyChallengeProgress cur = myRecordReader.loadChallengeProgress(userId, challengeId);

        long goal = userGoalService.loadMyGoalPoints(userId);

        return MyChallengeSummaryResponse.of(
                cur.currentRank(),
                cur.currentScore(),
                cur.scoreDelta(),
                goal
        );
    }
}