package com.chip.board.challenge.application.port;

import com.chip.board.challenge.domain.Challenge;

public interface ChallengeSavePort {
    Challenge save(Challenge challenge);
    Challenge getByIdForUpdate(long id); // finalize 같은 거 할 때(선택)
}
