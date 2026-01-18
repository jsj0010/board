package com.chip.board.syncproblem.application.service;

import com.chip.board.baselinesync.infrastructure.persistence.UserSolvedSyncStateJdbcRepository;
import com.chip.board.syncproblem.application.component.writer.ChallengeObserveJobWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeObserveEnqueueService {

    private final ChallengeObserveJobWriter observeWriter;
    private final UserSolvedSyncStateJdbcRepository stateRepo;

    public void enqueueObserveWindow(LocalDateTime windowStart) {
        // 필요한 유저 목록을 DB에서 한번에 가져오는 메서드가 필요합니다.
        // (기존 pickOneForObserve 반복 호출을 대체)
        List<Long> userIds = stateRepo.findObserveUserIds(windowStart);
        long now = System.currentTimeMillis();
        for (Long userId : userIds) {
            observeWriter.scheduleAt(userId, now);
        }
    }
}
