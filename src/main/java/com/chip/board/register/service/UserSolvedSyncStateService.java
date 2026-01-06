package com.chip.board.register.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.register.domain.User;
import com.chip.board.register.domain.UserSolvedSyncState;
import com.chip.board.register.repository.UserSolvedSyncStateRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserSolvedSyncStateService {

    private final UserSolvedSyncStateRepository syncStateRepository;

    @Transactional
    public void createInitialSyncState(User savedUser) {
        // 유저당 1행 보장(중복 방지)
        if (syncStateRepository.existsById(savedUser.getId())) {
            throw new ServiceException(ErrorCode.SYNC_STATE_ALREADY_EXISTS);
        }

        UserSolvedSyncState state = UserSolvedSyncState.builder()
                .user(savedUser)
                .build();

        syncStateRepository.save(state);
    }
}
