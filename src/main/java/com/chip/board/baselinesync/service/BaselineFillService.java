package com.chip.board.baselinesync.service;

import com.chip.board.baselinesync.infra.SolvedAcClient;
import com.chip.board.baselinesync.infra.UserSolvedProblemJdbcRepository;
import com.chip.board.baselinesync.infra.UserSolvedSyncStateJdbcRepository;
import com.chip.board.baselinesync.model.BaselineTarget;
import com.chip.board.baselinesync.model.SolvedAcSearchProblemResponse;
import com.chip.board.baselinesync.model.SolvedProblemItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaselineFillService {

    private final UserSolvedSyncStateJdbcRepository stateRepo;
    private final UserSolvedProblemJdbcRepository solvedRepo;
    private final SolvedAcClient solvedAc;
    private final TransactionTemplate tx;

    public void tickOnce() {
        if (solvedAc.isCooldownActive()) return;

        // 1) 짧은 트랜잭션: 대상 1명 잡기(락은 여기서만)
        Optional<BaselineTarget> baselineTargetOpt =
                tx.execute(status -> stateRepo.pickOneForBaseline());
        if (baselineTargetOpt == null || baselineTargetOpt.isEmpty()) {
            return;
        }

        BaselineTarget baselineTarget = baselineTargetOpt.get();

        String bojHandle = baselineTarget.bojHandle();
        long userId = baselineTarget.userId();
        int nextPage = baselineTarget.nextPage();

        if (bojHandle == null || bojHandle.isBlank()) {
            tx.executeWithoutResult(status -> stateRepo.markBaselineReady(userId)); // 큐에서 제거
            return;
        }

        // 2) 트랜잭션 밖: 외부 API 호출 (tick당 1회)
        SolvedAcSearchProblemResponse response = solvedAc.searchSolvedProblemsSafe(bojHandle, nextPage);
        if(response == null) return;

        List<SolvedAcSearchProblemResponse.Item> responseItems =
                (response.items() == null) ? List.of() : response.items();

        int totalSolvedCount = (response.count() == null) ? 0 : response.count();

        // 3) 짧은 트랜잭션: DB 반영
        tx.executeWithoutResult(status -> {
            stateRepo.initLastSolvedCountOnce(userId, totalSolvedCount);

            if (responseItems.isEmpty()) {
                stateRepo.markBaselineReady(userId);
                return;
            }

            List<SolvedProblemItem> solvedProblemItems = responseItems.stream()
                    .filter(item -> item.problemId() != null)
                    .filter(item -> item.level() != null)
                    .map(item -> new SolvedProblemItem(item.problemId(), item.level()))
                    .toList();

            if (solvedProblemItems.isEmpty()) {
                log.warn("All items filtered out, advancing page. userId={}, handle={}, nextPage={}, rawItems={}",
                        userId, bojHandle, nextPage, responseItems.size());
                stateRepo.advancePage(userId);
                return;
            }

            solvedRepo.upsertBatch(userId, solvedProblemItems, CreditedAtMode.SEAL_NOW);
            stateRepo.advancePage(userId);
        });
    }
}
