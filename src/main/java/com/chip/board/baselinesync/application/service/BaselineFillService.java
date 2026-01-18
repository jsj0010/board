package com.chip.board.baselinesync.application.service;

import com.chip.board.baselinesync.domain.CreditedAtMode;
import com.chip.board.baselinesync.application.component.reader.BaselineJobReader;
import com.chip.board.baselinesync.application.component.writer.BaselineJobWriter;
import com.chip.board.baselinesync.infrastructure.api.SolvedAcClient;
import com.chip.board.baselinesync.infrastructure.persistence.dto.BaselineTarget;
import com.chip.board.baselinesync.infrastructure.persistence.dto.SolvedProblemItem;
import com.chip.board.baselinesync.infrastructure.persistence.UserSolvedProblemJdbcRepository;
import com.chip.board.baselinesync.infrastructure.persistence.UserSolvedSyncStateJdbcRepository;
import com.chip.board.baselinesync.infrastructure.api.dto.response.SolvedAcSearchProblemResponse;
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

    private final BaselineJobReader jobReader;
    private final BaselineJobWriter jobWriter;

    /** 스케줄러에서 자주 호출해도 됨(전역 gate에서 대부분 return) */
    public void tickOnce() {
        if (solvedAc.isCooldownActive()) return;

        long now = System.currentTimeMillis();

        Optional<Long> userIdOpt = jobReader.popDueUserId(now);
        if (userIdOpt.isEmpty()) return;

        long userId = userIdOpt.get();

        // ✅ DB 폴링 제거: userId 1명만 조회
        Optional<BaselineTarget> targetOpt =
                tx.execute(status -> stateRepo.findBaselineTarget(userId));
        if (targetOpt == null || targetOpt.isEmpty()) return;

        BaselineTarget target = targetOpt.get();
        String bojHandle = target.bojHandle();
        int nextPage = target.nextPage();

        if (bojHandle == null || bojHandle.isBlank()) {
            tx.executeWithoutResult(status -> stateRepo.markBaselineReady(userId));
            return;
        }

        SolvedAcSearchProblemResponse response = solvedAc.searchSolvedProblemsSafe(bojHandle, nextPage);
        if (response == null) {
            jobWriter.scheduleAt(userId, solvedAc.nextAllowedAtMs());
            return;
        }

        List<SolvedAcSearchProblemResponse.Item> items =
                (response.items() == null) ? List.of() : response.items();
        int totalSolvedCount = (response.count() == null) ? 0 : response.count();

        boolean needMore = Boolean.TRUE.equals(
                tx.execute(status -> {
                    stateRepo.initLastSolvedCountOnce(userId, totalSolvedCount);

                    if (items.isEmpty()) {
                        stateRepo.markBaselineReady(userId);
                        return false;
                    }

                    List<SolvedProblemItem> solvedItems = items.stream()
                            .filter(it -> it.problemId() != null)
                            .filter(it -> it.level() != null)
                            .map(it -> new SolvedProblemItem(it.problemId(), it.level()))
                            .toList();

                    if (solvedItems.isEmpty()) {
                        log.warn("All items filtered out, advancing page. userId={}, handle={}, nextPage={}, rawItems={}",
                                userId, bojHandle, nextPage, items.size());
                        stateRepo.advancePage(userId);
                        return true;
                    }

                    solvedRepo.upsertBatch(userId, solvedItems, CreditedAtMode.SEAL_NOW);
                    stateRepo.advancePage(userId);
                    return true;
                })
        );


        if (needMore) {
            jobWriter.scheduleAt(userId, solvedAc.nextAllowedAtMs());
        }
    }
}