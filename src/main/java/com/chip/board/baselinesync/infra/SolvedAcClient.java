package com.chip.board.baselinesync.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class SolvedAcClient {

    private final WebClient webClient;

    public SolvedAcClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://solved.ac/api/v3")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-solvedac-language", "ko")
                .build();
    }

    public SolvedAcUserShowResponse userShow(String handle) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/show")
                        .queryParam("handle", handle)
                        .build())
                .retrieve()
                .bodyToMono(SolvedAcUserShowResponse.class)
                .timeout(Duration.ofSeconds(4))
                .block();
    }

    public SolvedAcSearchProblemResponse searchSolvedProblems(String handle, int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/problem")
                        .queryParam("query", "solved_by:" + handle)
                        .queryParam("page", page)
                        .queryParam("sort", "id")
                        .queryParam("direction", "asc")
                        .build())
                .retrieve()
                .bodyToMono(SolvedAcSearchProblemResponse.class)
                .timeout(Duration.ofSeconds(6))
                .block();
    }

    public record SolvedAcUserShowResponse(Integer solvedCount) {}

    public record SolvedAcSearchProblemResponse(Integer count, List<Item> items) {
        public record Item(Integer problemId, Integer level) {}
    }
}