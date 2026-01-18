package com.chip.board.baselinesync.application.scheduler;

import com.chip.board.baselinesync.application.service.BaselineFillService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class BaselineFillScheduler {

    private final BaselineFillService service;
    private final Clock clock;

    @Scheduled(fixedDelay = 500, initialDelay = 1000)
    public void tick() {
        LocalTime now = LocalTime.now(clock);

        // 06:00 <= now < 23:00
        if (now.isBefore(LocalTime.of(6, 0)) || !now.isBefore(LocalTime.of(23, 0))) {
            return;
        }

        service.tickOnce();
    }
}