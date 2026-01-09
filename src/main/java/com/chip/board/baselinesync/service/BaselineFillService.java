package com.chip.board.baselinesync.service;

import com.chip.board.baselinesync.infra.SolvedAcClient;
import com.chip.board.baselinesync.infra.UserSolvedProblemJdbcRepository;
import com.chip.board.baselinesync.infra.UserSolvedSyncStateJdbcRepository;
import com.chip.board.baselinesync.model.SolvedProblemItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaselineFillService {

    private final UserSolvedSyncStateJdbcRepository stateRepo;
    private final UserSolvedProblemJdbcRepository solvedRepo;
    private final SolvedAcClient solvedAc;

    /**
     * - 단일 워커 스케줄러에서 5초마다 호출
     * - tick 1회당 solved.ac API 최대 1번 호출
     */
    @Transactional
    public void tickOnce() {
        var opt = stateRepo.pickOneForBaseline();
        if (opt.isEmpty()) return;

        var t = opt.get();
        if (t.bojHandle() == null || t.bojHandle().isBlank()) return;

        var resp = solvedAc.searchSolvedProblems(t.bojHandle(), t.nextPage()); // API 1회
        var items = (resp == null || resp.items() == null) ? List.<SolvedAcClient.SolvedAcSearchProblemResponse.Item>of() : resp.items();

        int totalCount = (resp == null || resp.count() == null) ? 0 : resp.count();
        stateRepo.initLastSolvedCountOnce(t.userId(), totalCount);

        if (items.isEmpty()) {
            stateRepo.markBaselineReady(t.userId());
            return;
        }

        var mapped = items.stream()
                .filter(it -> it.problemId() != null)
                .map(it -> new SolvedProblemItem(it.problemId(), it.level()))
                .toList();

        solvedRepo.upsertBatch(t.userId(), mapped);
        stateRepo.advancePage(t.userId());
    }
}