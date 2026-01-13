package com.chip.board.baselinesync.scheduler;

import com.chip.board.baselinesync.service.BaselineFillService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BaselineFillScheduler {

    private final BaselineFillService service;

    // 06:00~22:59 동안 5초마다 1 tick
    @Scheduled(cron = "*/5 * 6-22 * * *", zone = "Asia/Seoul")
    public void tick() {
        service.tickOnce(); // tick 1회당 API 최대 1번
    }
}
