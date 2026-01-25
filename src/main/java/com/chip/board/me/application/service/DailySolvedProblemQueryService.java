package com.chip.board.me.application.service;

import com.chip.board.challenge.application.port.ChallengeLoadPort;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.me.application.port.MeQueryPort;
import com.chip.board.me.presentation.dto.response.DailySolvedProblemsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailySolvedProblemQueryService {

    private final ChallengeLoadPort challengeLoadPort;
    private final MeQueryPort dailySolvedProblemQueryPort;
    private final Clock clock;

    public DailySolvedProblemsResponse getDailySolvedProblems(long userId, long challengeId, LocalDate date) {
        if (!challengeLoadPort.existsById(challengeId)) {
            throw new ServiceException(ErrorCode.CHALLENGE_NOT_FOUND);
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<MeQueryPort.Row> rows = dailySolvedProblemQueryPort.findByUserIdAndChallengeIdBetween(
                userId, challengeId, start, end
        );

        List<DailySolvedProblemsResponse.Item> items = rows.stream()
                .map((MeQueryPort.Row r) -> new DailySolvedProblemsResponse.Item(
                        r.problemId(),
                        r.titleKo(),
                        r.level(),
                        r.tierName(),
                        r.points(),
                        r.solvedAt()
                ))
                .toList();

        return new DailySolvedProblemsResponse(date, items.size(), items);
    }
}