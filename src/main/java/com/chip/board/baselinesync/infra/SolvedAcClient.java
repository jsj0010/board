package com.chip.board.baselinesync.infra;

import com.chip.board.baselinesync.model.SolvedAcSearchProblemResponse;
import com.chip.board.baselinesync.model.SolvedAcUserShowResponse;
import com.chip.board.cooldown.infra.ApiCooldownActiveException;
import com.chip.board.cooldown.infra.ExternalApiCooldownJdbcRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Slf4j
@Component
public class SolvedAcClient {

    private static final String API_KEY = "solved_ac";
    private static final int COOLDOWN_MINUTES = 15;

    private final RestClient restClient;
    private final ExternalApiCooldownJdbcRepository cooldownRepo;

    public SolvedAcClient(RestClient.Builder builder,
                          ExternalApiCooldownJdbcRepository cooldownRepo,
                          @Value("${solved-ac.base-url}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-solvedac-language", "ko")
                .build();

        this.cooldownRepo = cooldownRepo;
    }

    public SolvedAcSearchProblemResponse searchSolvedProblemsSafe(String handle, int page) {
        try {
            return searchSolvedProblems(handle, page); // 기존 메서드(실제 호출) 재사용
        } catch (ApiCooldownActiveException e) {
            log.warn("API cooldown is active during the call: {}", e.getMessage());
            return null;
        } catch (RuntimeException e) {
            log.error("searchSolvedProblems API call failed for handle={}, page={}", handle, page, e);
            return null;
        }
    }

    public SolvedAcUserShowResponse userShowSafe(String handle) {
        try {
            return userShow(handle);
        } catch (ApiCooldownActiveException e) {
            log.warn("API cooldown is active during the call: {}", e.getMessage());
            return null;
        } catch (RuntimeException e) {
            log.error("userShow API call failed for handle={}", handle, e);
            return null;
        }
    }

    public boolean isCooldownActive() {
        return cooldownRepo.findActiveCooldownUntil(API_KEY, COOLDOWN_MINUTES) != null;
    }

    private SolvedAcUserShowResponse userShow(String handle) {
        assertNotCooldown();

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user/show")
                            .queryParam("handle", handle)
                            .build())
                    .retrieve()
                    .body(SolvedAcUserShowResponse.class);

        } catch (HttpClientErrorException e) {
            throw handleRateLimitThenRethrow(e);
        }
    }

    private SolvedAcSearchProblemResponse searchSolvedProblems(String handle, int page) {
        assertNotCooldown();

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/problem")
                            .queryParam("query", "solved_by:" + handle)
                            .queryParam("page", page)
                            .queryParam("sort", "id")
                            .queryParam("direction", "asc")
                            .build())
                    .retrieve()
                    .body(SolvedAcSearchProblemResponse.class);

        } catch (HttpClientErrorException e) {
            throw handleRateLimitThenRethrow(e);
        }
    }

    private void assertNotCooldown() {
        LocalDateTime until = cooldownRepo.findActiveCooldownUntil(API_KEY, COOLDOWN_MINUTES);
        if (until != null) {
            throw new ApiCooldownActiveException(API_KEY, until);
        }
    }

    private RuntimeException handleRateLimitThenRethrow(HttpClientErrorException e) {
        if (e.getStatusCode().value() == 429) {
            cooldownRepo.openCooldownNow(API_KEY, 429);
            LocalDateTime until = cooldownRepo.findActiveCooldownUntil(API_KEY, COOLDOWN_MINUTES);
            throw new ApiCooldownActiveException(
                    API_KEY,
                    until != null ? until : LocalDateTime.now().plusMinutes(COOLDOWN_MINUTES)
            );
        }
        return e;
    }
}
