package com.chip.board.register.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.register.domain.Department;
import com.chip.board.register.domain.Role;
import com.chip.board.register.domain.User;
import com.chip.board.register.dto.request.UserRegisterRequest;
import com.chip.board.register.dto.request.MailVerifyRequest;
import com.chip.board.register.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegisterService {

    private final StringRedisTemplate stringRedisTemplate;


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSolvedSyncStateService userSolvedSyncStateService;

    public List<String> showRegisterForm() {
        return Department.showDepartment();
    }

    @Transactional
    public void register(UserRegisterRequest userRegisterRequest) {
        String key = "auth:email:" + userRegisterRequest.getUsername();
        String verificationStatus = stringRedisTemplate.opsForValue().get(key);
        if (!"VERIFIED".equals(verificationStatus)) { // 이메일 인증이 아직 완료되지 않았을 때
            throw new ServiceException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        try {
            String encodedPassword=passwordEncoder.encode(userRegisterRequest.getPassword());
            Department departmentEnum = Department.fromDisplayName(userRegisterRequest.getDepartment());
            User user = User.builder()
                    .username(userRegisterRequest.getUsername())
                    .password(encodedPassword)
                    .name(userRegisterRequest.getName())
                    .department(departmentEnum.getDisplayName())
                    .studentId(userRegisterRequest.getStudentId())
                    .grade(userRegisterRequest.getGrade())
                    .role(Role.USER)
                    .bojId(userRegisterRequest.getBojId())
                    .phoneNumber(userRegisterRequest.getPhoneNumber())
                    .build();
            userRepository.save(user);
            userSolvedSyncStateService.createInitialSyncState(user);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    stringRedisTemplate.delete(key);
                }
            });
        }
        catch (DataIntegrityViolationException e) {
            throw mapDuplicateKeyToServiceException(e);
        }
    }

    private ServiceException mapDuplicateKeyToServiceException(DataIntegrityViolationException e) {
        String msg = getDeepMessage(e);

        if (msg.contains("uk_user_boj_id")) {
            return new ServiceException(ErrorCode.DUPLICATE_BOJ_ID);
        }
        if (msg.contains("uk_user_student_id")) {
            return new ServiceException(ErrorCode.DUPLICATE_STUDENT_ID);
        }
        if (msg.contains("uk_user_phone_number")) {
            return new ServiceException(ErrorCode.DUPLICATE_PHONE_NUMBER);
        }
        if (msg.contains("uk_user_username")) {
            return new ServiceException(ErrorCode.USER_ALREADY_EXIST);
        }

        return new ServiceException(ErrorCode.UNEXPECTED_SERVER_ERROR);
    }

    private String getDeepMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();

        if (cur instanceof SQLException sqlEx && sqlEx.getMessage() != null) {
            return sqlEx.getMessage();
        }
        return (cur.getMessage() != null) ? cur.getMessage() : t.toString();
    }
}


