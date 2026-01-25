package com.chip.board.challenge.application.service;

import com.chip.board.challenge.application.port.ChallengeLoadPort;
import com.chip.board.challenge.application.port.dto.ChallengeRankingAggregate;
import com.chip.board.challenge.application.port.ChallengeSavePort;
import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.presentation.dto.request.ChallengeCreateRequest;
import com.chip.board.challenge.presentation.dto.response.ChallengeInfoResponse;
import com.chip.board.challenge.presentation.dto.response.ChallengeDetailInfoResponse;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChallengeCommandService {

    private final ChallengeLoadPort challengeLoadPort;
    private final ChallengeSavePort challengeSavePort;

    @Transactional
    public ChallengeInfoResponse hold(ChallengeCreateRequest req) {
        if (!req.startDate().isBefore(req.endDate())) {
            throw new ServiceException(ErrorCode.CHALLENGE_RANGE_INVALID);
        }

        LocalDateTime startAt = req.startDate().atStartOfDay();          // 00:00
        LocalDateTime endAt = req.endDate().plusDays(1).atStartOfDay();  // 다음날 00:00

        // 활성/예약 챌린지 하나로만 강제
        if (challengeLoadPort.existsAnyOpen()) {
            throw new ServiceException(ErrorCode.CHALLENGE_ALREADY_EXISTS);
        }

        if (challengeLoadPort.existsOverlappingRange(startAt, endAt)) {
            throw new ServiceException(ErrorCode.CHALLENGE_RANGE_OVERLAPS);
        }

        Challenge saved = challengeSavePort.save(new Challenge(req.title(), startAt, endAt));
        return ChallengeInfoResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ChallengeInfoResponse getInfo(Long challengeId) {
        Challenge challenge = challengeLoadPort.findById(challengeId)
                .orElseThrow(() -> new ServiceException(ErrorCode.CHALLENGE_NOT_FOUND));

        return new ChallengeInfoResponse(
                challenge.getTitle(),
                challenge.getStartAt(),
                challenge.getEndAt(),
                challenge.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public ChallengeDetailInfoResponse getDetailInfo(Long challengeId) {
        Challenge challenge = challengeLoadPort.findById(challengeId)
                .orElseThrow(() -> new ServiceException(ErrorCode.CHALLENGE_NOT_FOUND));

        ChallengeRankingAggregate agg = challengeLoadPort.getRankingAggregate(challengeId);

        return new ChallengeDetailInfoResponse(
                challenge.getTitle(),
                challenge.getStartAt(),
                challenge.getEndAt(),
                challenge.getStatus(),
                agg.totalUserCount(),
                agg.participantsCount(),
                agg.totalSolvedCount(),
                agg.lastUpdatedAt()
        );
    }
}
