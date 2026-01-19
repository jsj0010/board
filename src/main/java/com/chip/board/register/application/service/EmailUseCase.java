package com.chip.board.register.application.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.register.application.command.VerifyEmailCommand;
import com.chip.board.register.infrastructure.persistence.repository.UserRepository;
import com.chip.board.register.application.port.EmailSender;
import com.chip.board.register.application.port.VerificationCodeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailUseCase {
    private final VerificationCodeStore verificationCodeStore;
    private final EmailSender emailSender;
    private final UserRepository userRepository;

    @Value("${verification.code.expiry-minutes}")
    private int expiryMinutes;

    public void sendAuthCode(String email) {
        if (userRepository.findByUsername(email).isPresent()) {
            throw new ServiceException(ErrorCode.USER_ALREADY_EXIST);
        }

        int code = new SecureRandom().nextInt(900000) + 100000;
        verificationCodeStore.saveAuthCode(email, code, Duration.ofMinutes(expiryMinutes));
        emailSender.sendAuthCode(email, code);
    }

    public void verifyCode(VerifyEmailCommand cmd) {
        String stored = verificationCodeStore.get(cmd.email());
        if (stored == null) throw new ServiceException(ErrorCode.EXPIRED_EMAIL_CODE);
        if ("VERIFIED".equals(stored)) return;
        if (!String.valueOf(cmd.code()).equals(stored)) {
            throw new ServiceException(ErrorCode.INVALID_EMAIL_CODE);
        }
        verificationCodeStore.markVerified(cmd.email(), Duration.ofMinutes(expiryMinutes));
    }

    public void sendTempPassword(String email, String tempPassword) {
        emailSender.sendTempPassword(email, tempPassword);
    }
}