package com.chip.board.baselinesync.application.port.solvedproblem;

import com.chip.board.baselinesync.domain.CreditedAtMode;
import com.chip.board.baselinesync.infrastructure.persistence.dto.SolvedProblemItem;

import java.util.List;

public interface SolvedProblemPort {
    void upsertBatch(long userId, List<SolvedProblemItem> items, CreditedAtMode mode);
}
