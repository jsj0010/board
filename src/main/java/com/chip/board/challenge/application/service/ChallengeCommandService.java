package com.chip.board.challenge.application.service;

import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.presentation.dto.request.ChallengeCreateRequest;
import com.chip.board.challenge.presentation.dto.response.ChallengeInfoResponse;
import com.chip.board.challenge.infrastructure.persistence.ChallengeRepository;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeCommandService {

    private final ChallengeRepository challengeRepository;

    @Transactional
    public ChallengeInfoResponse hold(ChallengeCreateRequest req) {
        if (!req.startDate().isBefore(req.endDate())) {
            throw new ServiceException(ErrorCode.CHALLENGE_RANGE_INVALID);
        }

        LocalDateTime startAt = req.startDate().atStartOfDay(); // 00:00
        LocalDateTime endAt = req.endDate().plusDays(1).atStartOfDay(); // 다음날 00:00

        //활성 챌린지 하나로만 강제
        if (challengeRepository.existsByStatusIn(List.of(ChallengeStatus.SCHEDULED, ChallengeStatus.ACTIVE))) {
            throw new ServiceException(ErrorCode.CHALLENGE_ALREADY_EXISTS);
        }

        if (challengeRepository.existsOverlappingRange(startAt, endAt)) {
            throw new ServiceException(ErrorCode.CHALLENGE_RANGE_OVERLAPS);
        }

        Challenge saved = challengeRepository.save(
                new Challenge(req.title(), startAt, endAt)
        );

        return ChallengeInfoResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ChallengeInfoResponse getInfo(Long challengeId) {
        Challenge c = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ServiceException(ErrorCode.CHALLENGE_NOT_FOUND));

        return new ChallengeInfoResponse(
                c.getTitle(),
                c.getStartAt(),
                c.getEndAt(),
                c.getStatus()
        );
    }
}