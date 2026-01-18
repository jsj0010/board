package com.chip.board.syncproblem.application.service;

import com.chip.board.baselinesync.domain.CreditedAtMode;
import com.chip.board.baselinesync.infrastructure.api.SolvedAcClient;
import com.chip.board.baselinesync.infrastructure.persistence.*;
import com.chip.board.baselinesync.infrastructure.api.dto.response.SolvedAcSearchProblemResponse;
import com.chip.board.baselinesync.infrastructure.api.dto.response.SolvedAcUserShowResponse;
import com.chip.board.baselinesync.infrastructure.persistence.dto.DeltaPageTarget;
import com.chip.board.baselinesync.infrastructure.persistence.dto.SolvedProblemItem;
import com.chip.board.baselinesync.infrastructure.persistence.dto.SyncTarget;
import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.infrastructure.persistence.ChallengeRepository;
import com.chip.board.syncproblem.infrastructure.persistence.ScoreEventJdbcRepository;
import com.chip.board.syncproblem.application.component.reader.ChallengeDeltaJobReader;
import com.chip.board.syncproblem.application.component.reader.ChallengeObserveJobReader;
import com.chip.board.syncproblem.application.component.writer.ChallengeDeltaJobWriter;
import com.chip.board.syncproblem.application.component.writer.ChallengeObserveJobWriter;
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

    private final ChallengeObserveJobReader observeReader;
    private final ChallengeObserveJobWriter observeWriter;
    private final ChallengeDeltaJobReader deltaReader;
    private final ChallengeDeltaJobWriter deltaWriter;

    public void tickOnce(LocalDateTime windowStart) {
        boolean cooldownActive = solvedAc.isCooldownActive();

        Optional<Challenge> challengeOpt = pickChallenge();
        if (challengeOpt.isEmpty()) return;

        Challenge challenge = challengeOpt.get();
        if (challenge.getStatus() == ChallengeStatus.CLOSED && challenge.isCloseFinalized()) return;

        CreditedAtMode upsertMode = decideUpsertMode(challenge);
        boolean isScoringPhase = isScoringPhase(challenge);

        long now = System.currentTimeMillis();

        if (!cooldownActive) {
            // 1) observe (API 1회)
            Optional<Long> observeUserIdOpt = observeReader.popDueUserId(now);
            if (observeUserIdOpt.isPresent()) {
                long userId = observeUserIdOpt.get();

                Optional<SyncTarget> tOpt = tx.execute(status -> stateRepo.findObserveTarget(userId, windowStart));
                if (tOpt == null || tOpt.isEmpty()) return;

                SyncTarget t = tOpt.get();

                SolvedAcUserShowResponse userShow = solvedAc.userShowSafe(t.bojHandle());
                if (userShow == null || userShow.solvedCount() == null) {
                    observeWriter.scheduleAt(userId, solvedAc.nextAllowedAtMs());
                    return;
                }

                Integer solvedCount = userShow.solvedCount();

                // observed 반영 → delta pending 판단(기존 DB 로직 유지)
                tx.executeWithoutResult(status -> stateRepo.updateObserved(userId, solvedCount));

                // delta가 필요한 상태면 delta 큐에 올림(필요 메서드: findDeltaTarget)
                Optional<DeltaPageTarget> deltaOpt =
                        tx.execute(status -> stateRepo.findDeltaTarget(userId, windowStart));
                if (deltaOpt != null && deltaOpt.isPresent()) {
                    deltaWriter.scheduleAt(userId, solvedAc.nextAllowedAtMs());
                }
                return; // tick당 API 1회
            }

            // 2) delta page sync (API 1회)
            Optional<Long> deltaUserIdOpt = deltaReader.popDueUserId(now);
            if (deltaUserIdOpt.isPresent()) {
                long userId = deltaUserIdOpt.get();

                Optional<DeltaPageTarget> deltaOpt =
                        tx.execute(status -> stateRepo.findDeltaTarget(userId, windowStart));
                if (deltaOpt == null || deltaOpt.isEmpty()) return;

                DeltaPageTarget delta = deltaOpt.get();

                SolvedAcSearchProblemResponse resp =
                        solvedAc.searchSolvedProblemsSafe(delta.bojHandle(), delta.nextPage());
                if (resp == null) {
                    deltaWriter.scheduleAt(userId, solvedAc.nextAllowedAtMs());
                    return;
                }

                List<SolvedAcSearchProblemResponse.Item> items =
                        (resp.items() == null) ? List.of() : resp.items();

                boolean needMore = Boolean.TRUE.equals(
                        tx.execute(status -> {
                            if (items.isEmpty()) {
                                stateRepo.finishDelta(delta.userId(), delta.observedSolvedCount());
                                return false;
                            }

                            List<SolvedProblemItem> solvedItems = items.stream()
                                    .filter(it -> it.problemId() != null)
                                    .filter(it -> it.level() != null)
                                    .map(it -> new SolvedProblemItem(it.problemId(), it.level()))
                                    .toList();

                            if (!solvedItems.isEmpty()) {
                                solvedRepo.upsertBatch(delta.userId(), solvedItems, upsertMode);
                            }

                            stateRepo.advancePage(delta.userId());
                            return true;
                        })
                );


                if (needMore) {
                    deltaWriter.scheduleAt(userId, solvedAc.nextAllowedAtMs());
                }
                return; // tick당 API 1회
            }
        }
        // 3) scoring (DB만) - 기존 유지
        if (isScoringPhase) {
            Boolean didScore = tx.execute(status -> {
                Optional<Long> scoreTargetUserIdOpt = stateRepo.pickOneForScoring();
                if (scoreTargetUserIdOpt.isEmpty()) return false;

                long userId = scoreTargetUserIdOpt.get();
                scoreRepo.insertScoreEventsForUncredited(challenge.getChallengeId(), userId);
                scoreRepo.fillCreditedAtFromScoreEvent(userId);
                return true;
            });

            // 실제로 1건 처리했을 때만 tick 종료
            if (Boolean.TRUE.equals(didScore)) return;
            // 처리할 게 없으면 finalize로 계속 진행
        }

        // 4) finalize (DB만) - 기존 유지
        tx.executeWithoutResult(status -> {
            Challenge managed = challengeRepo.findById(challenge.getChallengeId()).orElseThrow();

            if (managed.getStatus() == ChallengeStatus.ACTIVE && !managed.isPrepareFinalized()) {
                boolean observePendingExists = stateRepo.existsObservePending(windowStart);
                boolean deltaPendingExists = stateRepo.existsDeltaPending(windowStart);
                if (!observePendingExists && !deltaPendingExists) managed.finalizePrepare();
                return;
            }

            if (managed.getStatus() == ChallengeStatus.CLOSED && !managed.isCloseFinalized()) {
                boolean observePendingExists = stateRepo.existsObservePending(windowStart);
                boolean deltaPendingExists = stateRepo.existsDeltaPending(windowStart);
                boolean scoreableExists = stateRepo.existsAnyScoreable();
                if (!observePendingExists && !deltaPendingExists && !scoreableExists) managed.finalizeClose();
            }
        });
    }

    private Optional<Challenge> pickChallenge() {
        Optional<Challenge> active =
                challengeRepo.findFirstByStatus(ChallengeStatus.ACTIVE);
        if (active.isPresent()) return active;

        return challengeRepo.findTopByStatusAndCloseFinalizedFalseOrderByEndAtDesc(ChallengeStatus.CLOSED);
    }

    private static CreditedAtMode decideUpsertMode(Challenge c) {
        if (isScoringPhase(c)) return CreditedAtMode.SCOREABLE_NULL;
        return CreditedAtMode.SEAL_NOW;
    }

    private static boolean isScoringPhase(Challenge c) {
        return (c.getStatus() == ChallengeStatus.ACTIVE && c.isPrepareFinalized())
                || (c.getStatus() == ChallengeStatus.CLOSED && !c.isCloseFinalized());
    }
}