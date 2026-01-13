package com.chip.board.baselinesync.service;

import com.chip.board.baselinesync.infra.SolvedAcClient;
import com.chip.board.baselinesync.infra.UserSolvedProblemJdbcRepository;
import com.chip.board.baselinesync.infra.UserSolvedSyncStateJdbcRepository;
import com.chip.board.baselinesync.model.SolvedProblemItem;
import com.chip.board.cooldown.infra.ApiCooldownActiveException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaselineFillService {

    private final UserSolvedSyncStateJdbcRepository stateRepo;
    private final UserSolvedProblemJdbcRepository solvedRepo;
    private final SolvedAcClient solvedAc;
    private final TransactionTemplate tx;

    public void tickOnce() {
        // 1) 짧은 트랜잭션: 대상 1명 잡기(락은 여기서만)
        var opt = tx.execute(status -> stateRepo.pickOneForBaseline());
        if (opt == null || opt.isEmpty()) return;

        var t = opt.get();

        if (t.bojHandle() == null || t.bojHandle().isBlank()) {
            log.error("bojHandle is null/blank. userId={}", t.userId());
            tx.executeWithoutResult(s -> stateRepo.markBaselineReady(t.userId())); // 큐에서 제거
            return;
        }

        // 2) 트랜잭션 밖: 외부 API 호출 (tick당 1회)
        SolvedAcClient.SolvedAcSearchProblemResponse resp;
        try {
            resp = solvedAc.searchSolvedProblems(t.bojHandle(), t.nextPage());
        } catch (ApiCooldownActiveException e) {
            log.warn("Solved.ac cooldown active. until={}", e.until());
            return;
        } catch (RuntimeException e) {
            log.error("solved.ac call failed. userId={}, handle={}, nextPage={}",
                    t.userId(), t.bojHandle(), t.nextPage(), e);
            return; // 다음 tick 재시도
        }

        if (resp == null) {
            log.warn("solved.ac returned null body. userId={}, handle={}, nextPage={}",
                    t.userId(), t.bojHandle(), t.nextPage());
            return;
        }

        var items = (resp.items() == null)
                ? List.<SolvedAcClient.SolvedAcSearchProblemResponse.Item>of()
                : resp.items();

        int totalCount = (resp.count() == null) ? 0 : resp.count();

        // 3) 짧은 트랜잭션: DB 반영
        tx.executeWithoutResult(status -> {
            stateRepo.initLastSolvedCountOnce(t.userId(), totalCount);

            if (items.isEmpty()) {
                stateRepo.markBaselineReady(t.userId());
                return;
            }

            var mapped = items.stream()
                    .filter(it -> it.problemId() != null)
                    .filter(it -> it.level() != null)
                    .map(it -> new SolvedProblemItem(it.problemId(), it.level()))
                    .toList();

            if (mapped.isEmpty()) {
                log.warn("All items filtered out. will retry. userId={}, handle={}, nextPage={}, rawItems={}",
                        t.userId(), t.bojHandle(), t.nextPage(), items.size());
                stateRepo.advancePage(t.userId());
                return;
            }

            solvedRepo.upsertBatch(t.userId(), mapped, CreditedAtMode.SEAL_NOW);
            stateRepo.advancePage(t.userId());
        });
    }
}
