package com.chip.board.baselinesync.infrastructure.api;

import com.chip.board.baselinesync.infrastructure.api.dto.response.SolvedAcSearchProblemResponse;
import com.chip.board.baselinesync.infrastructure.api.dto.response.SolvedAcUserShowResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class SolvedAcClient {

    private static final String GATE_KEY = "solvedac:gate:next_allowed_at_ms";

    private static final long OK_INTERVAL_MS = 5_000L;
    private static final long COOLDOWN_429_MS = Duration.ofMinutes(15).toMillis();
    private static final long BACKOFF_5XX_NET_MS = 30_000L;

    private final RestClient restClient;
    private final StringRedisTemplate redis;

    public SolvedAcClient(RestClient.Builder builder,
                          StringRedisTemplate redis,
                          @Value("${solved-ac.base-url}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-solvedac-language", "ko")
                .build();
        this.redis = redis;
    }

    /** 전역 gate 활성 여부 */
    public boolean isCooldownActive() {
        return System.currentTimeMillis() < nextAllowedAtMs();
    }

    /** 다음 호출 가능 시각(epoch ms) */
    public long nextAllowedAtMs() {
        String v = redis.opsForValue().get(GATE_KEY);
        if (v == null) return 0L;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return 0L; }
    }

    private void setGateMs(long atMs) {
        redis.opsForValue().set(GATE_KEY, Long.toString(atMs));
    }

    private void afterOk(long now) { setGateMs(now + OK_INTERVAL_MS); }
    private void after429(long now) { setGateMs(now + COOLDOWN_429_MS); }
    private void afterTransient(long now) { setGateMs(now + BACKOFF_5XX_NET_MS); }

    public SolvedAcSearchProblemResponse searchSolvedProblemsSafe(String handle, int page) {
        long now = System.currentTimeMillis();
        if (now < nextAllowedAtMs()) return null;

        try {
            SolvedAcSearchProblemResponse body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/problem")
                            .queryParam("query", "solved_by:" + handle)
                            .queryParam("page", page)
                            .queryParam("sort", "id")
                            .queryParam("direction", "asc")
                            .build())
                    .retrieve()
                    .body(SolvedAcSearchProblemResponse.class);

            afterOk(now);
            return body;

        } catch (HttpClientErrorException e) {
            int code = e.getStatusCode().value();
            if (code == 429) {
                after429(now);
            } else {
                // 단순 처리: 429 외 4xx도 다음 호출은 5초 뒤로만 밀고 null
                afterOk(now);
            }
            log.warn("searchSolvedProblems 4xx. handle={}, page={}, status={}", handle, page, code);
            return null;

        } catch (HttpServerErrorException e) {
            afterTransient(now);
            log.warn("searchSolvedProblems 5xx. handle={}, page={}, status={}", handle, page, e.getStatusCode().value());
            return null;

        } catch (ResourceAccessException e) {
            afterTransient(now);
            log.warn("searchSolvedProblems network. handle={}, page={}", handle, page, e);
            return null;

        } catch (RuntimeException e) {
            afterTransient(now);
            log.warn("searchSolvedProblems runtime. handle={}, page={}", handle, page, e);
            return null;
        }
    }

    public SolvedAcUserShowResponse userShowSafe(String handle) {
        long now = System.currentTimeMillis();
        if (now < nextAllowedAtMs()) return null;

        try {
            SolvedAcUserShowResponse body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user/show")
                            .queryParam("handle", handle)
                            .build())
                    .retrieve()
                    .body(SolvedAcUserShowResponse.class);

            afterOk(now);
            return body;

        } catch (HttpClientErrorException e) {
            int code = e.getStatusCode().value();
            if (code == 429) {
                after429(now);
            } else {
                afterOk(now);
            }
            log.warn("userShow 4xx. handle={}, status={}", handle, code);
            return null;

        } catch (HttpServerErrorException e) {
            afterTransient(now);
            log.warn("userShow 5xx. handle={}, status={}", handle, e.getStatusCode().value());
            return null;

        } catch (ResourceAccessException e) {
            afterTransient(now);
            log.warn("userShow network. handle={}", handle, e);
            return null;

        } catch (RuntimeException e) {
            afterTransient(now);
            log.warn("userShow runtime. handle={}", handle, e);
            return null;
        }
    }
}