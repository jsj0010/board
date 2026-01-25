package com.chip.board.challenge.infrastructure.persistence.adapter;

import com.chip.board.challenge.application.port.ChallengeLoadPort;
import com.chip.board.challenge.application.port.dto.ChallengeRankingAggregate;
import com.chip.board.challenge.application.port.ChallengeSavePort;
import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.infrastructure.persistence.repository.ChallengeRepository;
import com.chip.board.register.application.port.UserRepositoryPort;
import com.chip.board.syncproblem.application.port.dto.ChallengeSyncSnapshot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChallengePersistenceAdapter implements ChallengeLoadPort, ChallengeSavePort {

    private final ChallengeRepository challengeRepository;
    private final EntityManager em;

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public Optional<Challenge> findActive() {
        return challengeRepository.findFirstByStatus(ChallengeStatus.ACTIVE);
    }

    @Override
    public Optional<Challenge> findFirstOpen() {
        Optional<Challenge> active = findActive();
        if (active.isPresent()) return active;
        return challengeRepository.findTopByStatusOrderByStartAtAsc(ChallengeStatus.SCHEDULED);
    }

    @Override
    public Optional<Challenge> findById(Long id) {
        return challengeRepository.findById(id);
    }

    @Override
    public boolean existsAnyOpen() {
        return challengeRepository.existsByStatusIn(List.of(ChallengeStatus.ACTIVE, ChallengeStatus.SCHEDULED));
    }

    @Override
    public boolean existsOverlappingRange(LocalDateTime startAt, LocalDateTime endAt) {
        return challengeRepository.existsOverlappingRange(startAt, endAt);
    }

    @Override
    public boolean existsById(Long id){
        return challengeRepository.existsById(id);
    }

    @Override
    public Challenge save(Challenge challenge) {
        return challengeRepository.save(challenge);
    }

    @Override
    public Challenge getByIdForUpdate(long id) {
        Challenge c = em.find(Challenge.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (c == null) throw new IllegalArgumentException("Challenge not found. id=" + id);
        return c;
    }

    @Override
    public boolean existsActive() {
        return challengeRepository.existsByStatus(ChallengeStatus.ACTIVE);
    }

    @Override
    public boolean existsClosedUnfinalized() {
        return challengeRepository.existsByStatusAndCloseFinalized(ChallengeStatus.CLOSED, false);
    }

    @Override
    public Optional<ChallengeSyncSnapshot> findCurrentSyncTarget() {
        return challengeRepository.findFirstByStatus(ChallengeStatus.ACTIVE)
                .or(() -> challengeRepository.findTopByStatusAndCloseFinalizedFalseOrderByEndAtDesc(ChallengeStatus.CLOSED))
                .map(ChallengeSyncSnapshot::from);
    }

    @Override
    public ChallengeRankingAggregate getRankingAggregate(Long challengeId) {
        long totalUserCount = userRepositoryPort.countByDeletedFalse();

        ChallengeRepository.RankingSummaryAgg agg = challengeRepository.findRankingSummaryAgg(challengeId);

        long participantsCount = agg.getParticipantsCount();
        long totalSolvedCount = agg.getTotalSolvedCount();

        return new ChallengeRankingAggregate(
                totalUserCount,
                participantsCount,
                totalSolvedCount,
                agg.getLastUpdatedAt()
        );
    }
}