package com.chip.board.baselinesync.infrastructure.api.dto.response;

import java.util.List;

public record SolvedAcSearchProblemResponse(Integer count, List<Item> items) {
    public record Item(Integer problemId, Integer level) {}
}