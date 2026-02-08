package com.chip.board.me.application.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.register.domain.User;
import com.chip.board.register.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserGoalService {
    private final UserRepository userRepository;

    @Transactional
    public void updateMyGoalPoints(long userId, long goalPoints) {
        if (goalPoints < 0) throw new ServiceException(ErrorCode.INVALID_GOAL_POINTS);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        user.changeGoalPoints(goalPoints);
    }

    @Transactional(readOnly = true)
    public long loadMyGoalPoints(long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
        return user.getGoalPoints();
    }
}