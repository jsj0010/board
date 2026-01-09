package com.chip.board.baselinesync.service;

import com.chip.board.baselinesync.infra.SolvedAcClient;
import com.chip.board.baselinesync.infra.UserSolvedProblemJdbcRepository;
import com.chip.board.baselinesync.infra.UserSolvedSyncStateJdbcRepository;
import com.chip.board.baselinesync.model.SolvedProblemItem;
import com.chip.board.cooldown.infra.ApiCooldownActiveException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
        if (t.bojHandle() == null || t.bojHandle().isBlank()) {
            log.error("bojHandle is null/blank. userId={}", t.userId());
            stateRepo.markBaselineReady(t.userId()); // 큐에서 제거
            return;
        }

        SolvedAcClient.SolvedAcSearchProblemResponse resp;
        try {
            resp = solvedAc.searchSolvedProblems(t.bojHandle(), t.nextPage()); // API 1회
        } catch (ApiCooldownActiveException e) {
            log.warn("Solved.ac cooldown active. until={}", e.until());
            return;
        } catch (WebClientResponseException e) {
            // HTTP 응답이 왔는데 에러(4xx/5xx)인 케이스
            var status = e.getStatusCode();
            log.warn("solved.ac HTTP error. userId={}, handle={}, nextPage={}, status={}, body={}",
                    t.userId(), t.bojHandle(), t.nextPage(), status.value(),
                    safeBody(e), e);

            if (status.value() == 404) { // handle not found 성격이면
                stateRepo.markBaselineReady(t.userId());
                return;
            }
            throw e;
        } catch (RuntimeException e) {
            // 타임아웃/네트워크 등 런타임 예외
            log.error("solved.ac call failed. userId={}, handle={}, nextPage={}",
                    t.userId(), t.bojHandle(), t.nextPage(), e);
            throw e; // 롤백 → 다음 tick 재시도
        }

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

    private String safeBody(WebClientResponseException e) {
        try {
            var b = e.getResponseBodyAsString();
            return (b == null) ? "" : (b.length() > 300 ? b.substring(0, 300) + "..." : b);
        } catch (Exception ignore) {
            return "";
        }
    }
}