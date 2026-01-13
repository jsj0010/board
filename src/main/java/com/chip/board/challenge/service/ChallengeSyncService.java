package com.chip.board.challenge.service;

import com.chip.board.baselinesync.infra.SolvedAcClient;
import com.chip.board.baselinesync.infra.UserSolvedProblemJdbcRepository;
import com.chip.board.baselinesync.infra.UserSolvedSyncStateJdbcRepository;
import com.chip.board.baselinesync.model.SolvedProblemItem;
import com.chip.board.baselinesync.service.CreditedAtMode;
import com.chip.board.challenge.domain.Challenge;
import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.repository.ChallengeRepository;
import com.chip.board.challenge.repository.ScoreEventJdbcRepository;
import com.chip.board.cooldown.infra.ApiCooldownActiveException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
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
        Optional<Challenge> cOpt = pickChallenge();
        if (cOpt.isEmpty()) return;

        Challenge c = cOpt.get();
        if (c.getStatus() == ChallengeStatus.CLOSED && c.isCloseFinalized()) return;

        CreditedAtMode upsertMode = decideUpsertMode(c);
        boolean scoringPhase = isScoringPhase(c);

        // 1) observed (API 1회)
        try {
            var obsOpt = tx.execute(st -> stateRepo.pickOneForObserve(windowStart));
            if (obsOpt != null && obsOpt.isPresent()) {
                var t = obsOpt.get();
                var show = solvedAc.userShow(t.bojHandle()); // API 1회
                if (show != null && show.solvedCount() != null) {
                    tx.executeWithoutResult(st ->
                            stateRepo.updateObserved(t.userId(), show.solvedCount()));
                }
                return; // tick당 API 1회 보장
            }

            // 2) delta page sync (API 1회)
            var deltaOpt = tx.execute(st -> stateRepo.pickOneForDeltaPage(windowStart));
            if (deltaOpt != null && deltaOpt.isPresent()) {
                var t = deltaOpt.get();

                var resp = solvedAc.searchSolvedProblems(t.bojHandle(), t.nextPage()); // API 1회
                if (resp == null) {
                    log.warn("solved.ac returned null body. userId={}, handle={}, nextPage={}",
                            t.userId(), t.bojHandle(), t.nextPage());
                    return;
                }
                var items = (resp.items() == null) ? List.<SolvedAcClient.SolvedAcSearchProblemResponse.Item>of() : resp.items();

                tx.executeWithoutResult(st -> {
                    if (items.isEmpty()) {
                        stateRepo.finishDelta(t.userId(), t.observedSolvedCount());
                        return;
                    }

                    var mapped = items.stream()
                            .filter(it -> it.problemId() != null)
                            .filter(it -> it.level() != null)
                            .map(it -> new SolvedProblemItem(it.problemId(), it.level()))
                            .toList();

                    if (!mapped.isEmpty()) {
                        solvedRepo.upsertBatch(t.userId(), mapped, upsertMode);
                    }

                    // 다음 페이지로 진행(종료는 "빈 페이지"에서만)
                    stateRepo.advancePage(t.userId());
                });
                return; // tick당 API 1회 보장
            }

        } catch (ApiCooldownActiveException e) {
            // cooldown이면 이번 tick은 끝(다음 tick에서 재시도)
            log.warn("solved.ac cooldown active. until={}", e.until());
            return;
        } catch (RuntimeException e) {
            log.error("solved.ac call failed", e);
            return; // 다음 tick 재시도
        }

        // 3) scoring (DB만) - scoring 구간에만
        if (scoringPhase) {
            tx.executeWithoutResult(st -> {
                var userIdOpt = pickOneScoreTargetUserId(); 
                if (userIdOpt.isEmpty()) return;

                long userId = userIdOpt.get();

                scoreRepo.insertScoreEventsForUncredited(c.getChallengeId(), userId);
                scoreRepo.fillCreditedAtFromScoreEvent(userId);
            });
            return;
        }

        // 4) finalize (DB만)
        tx.executeWithoutResult(st -> {
            Challenge managed = challengeRepo.findById(c.getChallengeId()).orElseThrow();

            if (managed.getStatus() == ChallengeStatus.ACTIVE && !managed.isPrepareFinalized()) {
                boolean obsLeft = stateRepo.existsObservePending(windowStart);
                boolean deltaLeft = stateRepo.existsDeltaPending(windowStart);
                if (!obsLeft && !deltaLeft) managed.finalizePrepare();
                return;
            }

            if (managed.getStatus() == ChallengeStatus.CLOSED && !managed.isCloseFinalized()) {
                boolean obsLeft = stateRepo.existsObservePending(windowStart);
                boolean deltaLeft = stateRepo.existsDeltaPending(windowStart);
                boolean scoreLeft = existsAnyScoreable();
                if (!obsLeft && !deltaLeft && !scoreLeft) managed.finalizeClose();
            }
        });
    }

    private Optional<Challenge> pickChallenge() {
        // 한 시점에 ACTIVE가 최대 1개라는 전제면 이 방식이 단순/안전합니다.
        var active = challengeRepo.findFirstByStatusIn(List.of(ChallengeStatus.ACTIVE));
        if (active.isPresent()) return active;

        var closed = challengeRepo.findFirstByStatusIn(List.of(ChallengeStatus.CLOSED));
        if (closed.isPresent()) return closed;

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
