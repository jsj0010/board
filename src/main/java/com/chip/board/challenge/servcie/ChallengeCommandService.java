package com.chip.board.challenge.servcie;

import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.dto.request.ChallengeCreateRequest;
import com.chip.board.challenge.dto.response.ChallengeInfoResponse;
import com.chip.board.challenge.repository.ChallengeRepository;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeCommandService {

    private final ChallengeRepository challengeRepository;

    @Transactional
    public ChallengeInfoResponse hold(ChallengeCreateRequest req) {
        if (!req.startAt().isBefore(req.endAt())) {
            throw new ServiceException(ErrorCode.CHALLENGE_RANGE_INVALID);
        }

        if (challengeRepository.existsOverlappingRange(req.startAt(), req.endAt())) {
            throw new ServiceException(ErrorCode.CHALLENGE_RANGE_OVERLAPS);
        }

        Challenge saved = challengeRepository.save(
                new Challenge(req.title(), req.startAt(), req.endAt())
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