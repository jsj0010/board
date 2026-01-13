package com.chip.board.challenge.service;

import com.chip.board.baselinesync.infra.SolvedAcClient;
import com.chip.board.baselinesync.infra.UserSolvedProblemJdbcRepository;
import com.chip.board.baselinesync.infra.UserSolvedSyncStateJdbcRepository;
import com.chip.board.baselinesync.model.*;
import com.chip.board.baselinesync.service.CreditedAtMode;
import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.repository.ChallengeRepository;
import com.chip.board.challenge.repository.ScoreEventJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeSyncService {

    private final ChallengeRepository challengeRepo;
    private final UserSolvedSyncStateJdbcRepository stateRepo;
    private final UserSolvedProblemJdbcRepository solvedRepo;
    private final ScoreEventJdbcRepository scoreRepo;
    private final SolvedAcClient solvedAc;
    private final TransactionTemplate tx;

    public void tickOnce(LocalDateTime windowStart) {
        if (solvedAc.isCooldownActive()) return;

        Optional<Challenge> challengeOpt = pickChallenge();
        if (challengeOpt.isEmpty()) return;

        Challenge challenge = challengeOpt.get();
        if (challenge.getStatus() == ChallengeStatus.CLOSED && challenge.isCloseFinalized()) return;

        CreditedAtMode upsertMode = decideUpsertMode(challenge);
        boolean isScoringPhase = isScoringPhase(challenge);

        // 1) observed (API 1회)
        Optional<SyncTarget> observeTargetOpt =
                tx.execute(status -> stateRepo.pickOneForObserve(windowStart));
        if (observeTargetOpt != null && observeTargetOpt.isPresent()) {
            SyncTarget observeTarget = observeTargetOpt.get();

            SolvedAcUserShowResponse userShowResponse =
                    solvedAc.userShowSafe(observeTarget.bojHandle()); // API 1회

            Integer solvedCount = (userShowResponse == null) ? null : userShowResponse.solvedCount();
            if (solvedCount != null) {
                tx.executeWithoutResult(status ->
                        stateRepo.updateObserved(observeTarget.userId(), solvedCount));
            }
            return; // tick당 API 1회 보장
        }

        // 2) delta page sync (API 1회)
        Optional<DeltaPageTarget> deltaTargetOpt =
                tx.execute(status -> stateRepo.pickOneForDeltaPage(windowStart));
        if (deltaTargetOpt != null && deltaTargetOpt.isPresent()) {
            DeltaPageTarget deltaTarget = deltaTargetOpt.get();
            SolvedAcSearchProblemResponse searchResponse =
                    solvedAc.searchSolvedProblemsSafe(deltaTarget.bojHandle(), deltaTarget.nextPage()); // API 1회

            if (searchResponse == null) { return;}

            List<SolvedAcSearchProblemResponse.Item> responseItems = (searchResponse.items() == null)
                            ? List.<SolvedAcSearchProblemResponse.Item>of()
                                : searchResponse.items();

            tx.executeWithoutResult(status -> {
                if (responseItems.isEmpty()) {
                    stateRepo.finishDelta(deltaTarget.userId(), deltaTarget.observedSolvedCount());
                    return;
                }

                List<SolvedProblemItem> solvedProblemItems = responseItems.stream()
                        .filter(item -> item.problemId() != null)
                        .filter(item -> item.level() != null)
                        .map(item -> new SolvedProblemItem(item.problemId(), item.level()))
                        .toList();

                if (!solvedProblemItems.isEmpty()) {
                    solvedRepo.upsertBatch(deltaTarget.userId(), solvedProblemItems, upsertMode);
                }

                stateRepo.advancePage(deltaTarget.userId());
            });
            return; // tick당 API 1회 보장
        }

        // 3) scoring (DB만)
        if (isScoringPhase) {
            tx.executeWithoutResult(status -> {
                Optional<Long> scoreTargetUserIdOpt = pickOneScoreTargetUserId();
                if (scoreTargetUserIdOpt.isEmpty()) return;

                long scoreTargetUserId = scoreTargetUserIdOpt.get();
                scoreRepo.insertScoreEventsForUncredited(challenge.getChallengeId(), scoreTargetUserId);
                scoreRepo.fillCreditedAtFromScoreEvent(scoreTargetUserId);
            });
            return;
        }

        // 4) finalize (DB만)
        tx.executeWithoutResult(status -> {
            Challenge managedChallenge = challengeRepo.findById(challenge.getChallengeId()).orElseThrow();

            if (managedChallenge.getStatus() == ChallengeStatus.ACTIVE && !managedChallenge.isPrepareFinalized()) {
                boolean observePendingExists = stateRepo.existsObservePending(windowStart);
                boolean deltaPendingExists = stateRepo.existsDeltaPending(windowStart);
                if (!observePendingExists && !deltaPendingExists) managedChallenge.finalizePrepare();
                return;
            }

            if (managedChallenge.getStatus() == ChallengeStatus.CLOSED && !managedChallenge.isCloseFinalized()) {
                boolean observePendingExists = stateRepo.existsObservePending(windowStart);
                boolean deltaPendingExists = stateRepo.existsDeltaPending(windowStart);
                boolean scoreableExists = existsAnyScoreable();
                if (!observePendingExists && !deltaPendingExists && !scoreableExists) managedChallenge.finalizeClose();
            }
        });
    }

    private Optional<Challenge> pickChallenge() {
        Optional<Challenge> activeChallengeOpt =
                challengeRepo.findFirstByStatusIn(List.of(ChallengeStatus.ACTIVE));
        if (activeChallengeOpt.isPresent()) return activeChallengeOpt;

        Optional<Challenge> closedChallengeOpt =
                challengeRepo.findFirstByStatusIn(List.of(ChallengeStatus.CLOSED));
        if (closedChallengeOpt.isPresent()) return closedChallengeOpt;

        return challengeRepo.findFirstByStatusIn(List.of(ChallengeStatus.SCHEDULED));
    }


    private static CreditedAtMode decideUpsertMode(Challenge c) {
        // 점수 후보로 쌓을 수 있는 구간만 NULL
        if (isScoringPhase(c)) return CreditedAtMode.SCOREABLE_NULL;
        return CreditedAtMode.SEAL_NOW; // SCHEDULED, ACTIVE prepare 구간
    }

    private static boolean isScoringPhase(Challenge c) {
        return (c.getStatus() == ChallengeStatus.ACTIVE && c.isPrepareFinalized())
                || (c.getStatus() == ChallengeStatus.CLOSED && !c.isCloseFinalized());
    }

    private Optional<Long> pickOneScoreTargetUserId() {
        return stateRepo.pickOneForScoring();
    }

    private boolean existsAnyScoreable() {
        return stateRepo.existsAnyScoreable();
    }
}
