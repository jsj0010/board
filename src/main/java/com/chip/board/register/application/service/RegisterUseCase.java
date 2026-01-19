package com.chip.board.register.application.service;

import com.chip.board.baselinesync.application.port.baselineJob.BaselineEnqueuePort;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.register.application.command.RegisterUserCommand;
import com.chip.board.register.infrastructure.persistence.repository.UserRepository;
import com.chip.board.register.application.port.UserSolvedSyncPort;
import com.chip.board.register.application.port.VerificationCodeStore;
import com.chip.board.register.domain.Department;
import com.chip.board.register.domain.Role;
import com.chip.board.register.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {
    private final VerificationCodeStore verificationCodeStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSolvedSyncPort userSolvedSyncPort;
    private final BaselineEnqueuePort baselineEnqueuePort;

    @Transactional
    public void register(RegisterUserCommand cmd) {
        String stored = verificationCodeStore.get(cmd.username());
        if (!"VERIFIED".equals(stored)) {
            throw new ServiceException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        Department dept = Department.fromDisplayName(cmd.department());
        User user = User.builder()
                .username(cmd.username())
                .password(passwordEncoder.encode(cmd.password()))
                .name(cmd.name())
                .department(dept.getDisplayName())
                .studentId(cmd.studentId())
                .grade(cmd.grade())
                .role(Role.USER)
                .bojId(cmd.bojId())
                .build();

        userRepository.save(user);
        userSolvedSyncPort.createInitialSyncState(user);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                baselineEnqueuePort.enqueueBaseline(user.getId());
            }
        });
    }
}
