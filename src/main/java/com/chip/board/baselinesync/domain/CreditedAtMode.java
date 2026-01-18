package com.chip.board.baselinesync.domain;

public enum CreditedAtMode {
    SEAL_NOW,      // baseline / scheduled / ACTIVE prepare_finalized=false
    SCOREABLE_NULL // ACTIVE prepare_finalized=true / CLOSED close_finalized=false
}