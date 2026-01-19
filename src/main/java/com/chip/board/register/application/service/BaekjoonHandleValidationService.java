package com.chip.board.register.application.service;

import com.chip.board.baselinesync.application.port.api.SolvedAcPort;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.register.application.port.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BaekjoonHandleValidationService {

    private final SolvedAcPort solvedAcPort;
    private final UserRepositoryPort userRepositoryPort;

    @Transactional(readOnly = true)
    public boolean validate(String handle) {
        boolean alreadyUsed = userRepositoryPort.existsByBojId(handle);
        if (alreadyUsed) {
            throw new ServiceException(ErrorCode.BAEKJOON_HANDLE_ALREADY_USED);
        }

        boolean cooldownActive = solvedAcPort.isCooldownActive();
        if (cooldownActive) {
            throw new ServiceException(ErrorCode.SOLVEDAC_COOLDOWN_ACTIVE);
        }

        Integer solvedCount = solvedAcPort.fetchSolvedCountOrNull(handle);
        if (solvedCount == null) {
            throw new ServiceException(ErrorCode.BAEKJOON_HANDLE_INVALID);
        }
        return true;
    }
}