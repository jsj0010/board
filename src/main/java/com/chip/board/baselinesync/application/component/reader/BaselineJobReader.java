package com.chip.board.baselinesync.application.component.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BaselineJobReader {

    //단일 서버 운영
    public static final String KEY = "solvedac:baseline:jobs";
    private final StringRedisTemplate redis;

    public Optional<Long> popDueUserId(long nowMs) {
        ZSetOperations<String, String> zset = redis.opsForZSet();

        Set<ZSetOperations.TypedTuple<String>> first = zset.rangeWithScores(KEY, 0, 0);
        if (first == null || first.isEmpty()) return Optional.empty();

        ZSetOperations.TypedTuple<String> tuple = first.iterator().next();
        if (tuple == null || tuple.getValue() == null || tuple.getScore() == null) return Optional.empty();

        if (nowMs < tuple.getScore().longValue()) return Optional.empty();

        Set<ZSetOperations.TypedTuple<String>> popped = zset.popMin(KEY, 1);
        if (popped == null || popped.isEmpty()) return Optional.empty();

        String userIdStr = popped.iterator().next().getValue();
        if (userIdStr == null) return Optional.empty();

        try { return Optional.of(Long.parseLong(userIdStr)); }
        catch (NumberFormatException e) { return Optional.empty(); }
    }
}