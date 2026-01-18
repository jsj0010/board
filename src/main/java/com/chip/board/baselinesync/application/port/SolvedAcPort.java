package com.chip.board.baselinesync.application.port;

import com.chip.board.baselinesync.infrastructure.api.dto.response.SolvedAcSearchProblemResponse;
import com.chip.board.baselinesync.infrastructure.api.dto.response.SolvedAcUserShowResponse;

public interface SolvedAcPort {
    boolean isCooldownActive();
    long nextAllowedAtMs();
    SolvedAcUserShowResponse userShowSafe(String handle);
    SolvedAcSearchProblemResponse searchSolvedProblemsSafe(String handle, int page);
}
