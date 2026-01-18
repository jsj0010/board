package com.chip.board.syncproblem.application.component.writer;

import com.chip.board.syncproblem.application.component.reader.ChallengeDeltaJobReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeDeltaJobWriter {

    private final StringRedisTemplate redis;

    public void scheduleAt(long userId, long atMs) {
        redis.opsForZSet().add(ChallengeDeltaJobReader.KEY, Long.toString(userId), (double) atMs);
    }
}