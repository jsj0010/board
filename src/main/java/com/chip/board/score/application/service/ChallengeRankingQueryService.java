package com.chip.board.score.application.service;

import com.chip.board.challenge.application.port.ChallengeLoadPort;
import com.chip.board.challenge.domain.Challenge;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.register.application.port.UserRepositoryPort;
import com.chip.board.register.application.port.dto.ChallengeRankingRow;
import com.chip.board.score.presentation.dto.response.ChallengeRankingItemResponse;
import com.chip.board.score.presentation.dto.response.ChallengeRankingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeRankingQueryService {

    private final UserRepositoryPort userRepositoryPort;
    private final ChallengeLoadPort challengeLoadPort;

    public ChallengeRankingResponse getRankingsAllUsers(Long challengeId, int page, int size) {

        challengeLoadPort.findById(challengeId)
                .orElseThrow(() -> new ServiceException(ErrorCode.CHALLENGE_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);

        Page<ChallengeRankingRow> resultPage =
                userRepositoryPort.findRankingsAllUsers(challengeId, pageable);

        List<ChallengeRankingItemResponse> items =
                resultPage.getContent().stream()
                        .map((ChallengeRankingRow r) -> new ChallengeRankingItemResponse(
                                r.currentRankNo(),
                                r.name(),
                                r.bojId(),
                                r.department(),
                                r.solvedCount(),
                                r.totalPoints(),
                                r.delta()
                        ))
                        .toList();

        Instant generatedAt = Instant.now();

        boolean hasNext = resultPage.hasNext();
        int nextPage = hasNext ? resultPage.getNumber() + 1 : -1;

        return new ChallengeRankingResponse(
                challengeId,
                generatedAt,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                hasNext,
                nextPage,
                items
        );
    }
}