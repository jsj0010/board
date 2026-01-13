package com.chip.board.baselinesync.model;

import java.util.List;

public record SolvedAcSearchProblemResponse(Integer count, List<Item> items) {
    public record Item(Integer problemId, Integer level) {}
}