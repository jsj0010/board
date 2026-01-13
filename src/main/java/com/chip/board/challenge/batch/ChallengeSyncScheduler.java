package com.chip.board.challenge.batch;

import com.chip.board.challenge.service.ChallengeSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChallengeSyncScheduler {

    private final ChallengeSyncService service;
    private final Clock clock;

    // 매일 00:01:00 ~ 03:59 동안 5초마다
    @Scheduled(cron = "0/5 1-59 0 * * *", zone = "Asia/Seoul") // 00:01:00 ~ 00:59:55
    @Scheduled(cron = "0/5 * 1-3 * * *", zone = "Asia/Seoul")   // 01:00:00 ~ 03:59:55
    public void tick() {
        LocalDateTime windowStart = LocalDate.now(clock).atTime(0, 1, 0);
        service.tickOnce(windowStart);
    }
}
