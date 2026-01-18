package com.chip.board.syncproblem.application.component.writer;

import com.chip.board.syncproblem.application.component.reader.ChallengeObserveJobReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeObserveJobWriter {

    private final StringRedisTemplate redis;

    public void scheduleAt(long userId, long atMs) {
        redis.opsForZSet().add(ChallengeObserveJobReader.KEY, Long.toString(userId), (double) atMs);
    }

    public void scheduleNow(long userId) {
        scheduleAt(userId, System.currentTimeMillis());
    }
}
