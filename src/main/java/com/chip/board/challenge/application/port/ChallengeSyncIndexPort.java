package com.chip.board.challenge.application.port;

import com.chip.board.syncproblem.application.port.dto.ChallengeSyncSnapshot;

import java.util.Optional;

public interface ChallengeSyncIndexPort {
    Optional<ChallengeSyncSnapshot> load();
    void save(ChallengeSyncSnapshot snapshot);
    void delete();
}
