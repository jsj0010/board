package com.chip.board.syncproblem.application.scheduler;

import com.chip.board.syncproblem.application.service.ChallengeSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class ChallengeSyncScheduler {

    private final ChallengeSyncService challengeSyncService;
    private final Clock clock;

    // 00:01 ~ 02:59 동안만 돌리고, 그 외 시간엔 즉시 return
    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    public void tick() {
        LocalTime now = LocalTime.now(clock);

        // 00:01 <= now < 03:00
        if (now.isBefore(LocalTime.of(0, 2)) || !now.isBefore(LocalTime.of(3, 0))) {
            return;
        }

        LocalDateTime windowStart = LocalDate.now(clock).atTime(0, 2, 0);
        challengeSyncService.tickOnce(windowStart); // tick 1회당 API 최대 1번
    }
}
