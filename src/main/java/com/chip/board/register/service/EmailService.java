package com.chip.board.register.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.register.dto.request.MailVerifyRequest;
import com.chip.board.register.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;

    @Value("${verification.code.expiry-minutes}")
    private int verificationCodeExpiryMinutes;

    @Value("${email.sender}")
    private String senderEmail;

    private static final String PREFIX_EMAIL_SUBJECT = "[CHIP_SAT] ";

    private static final String AUTH_CODE_EMAIL_SUBJECT = PREFIX_EMAIL_SUBJECT + "인증번호 발송";
    private static final String AUTH_CODE_EMAIL_BODY =
            """
            <h3>요청하신 인증 번호입니다.</h3>
            <h1>%s</h1>
            <h3>감사합니다.</h3>
            """;

    public static final String TEMP_PASSWORD_EMAIL_SUBJECT = PREFIX_EMAIL_SUBJECT + "임시 비밀번호 안내";
    public static final String TEMP_PASSWORD_EMAIL_BODY =
            """
            <h3>요청하신 임시 비밀번호입니다.</h3>
            <h1>%s</h1>
            <h3>보안을 위해 로그인 후 반드시 비밀번호를 변경해 주세요!</h3>
            <h3>감사합니다.</h3>
            """;

    @Async
    public CompletableFuture<Boolean> sendAuthCodeMailAsync(String email, int number) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject(AUTH_CODE_EMAIL_SUBJECT);
            String body = String.format(AUTH_CODE_EMAIL_BODY, number);
            message.setText(body,"UTF-8", "html");
            javaMailSender.send(message);
            return CompletableFuture.completedFuture(true);
        } catch (MessagingException e) {
            return CompletableFuture.failedFuture(new ServiceException(ErrorCode.EMAIL_SEND_ERROR));
        }
    }


    @Async
    public void sendPasswordMail(String username, String password) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, username);
            message.setSubject(TEMP_PASSWORD_EMAIL_SUBJECT);
            String body = String.format(TEMP_PASSWORD_EMAIL_BODY, password);
            message.setText(body, "UTF-8", "html");
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new ServiceException(ErrorCode.EMAIL_SEND_ERROR);
        }
    }

    public void sendMail(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ServiceException(ErrorCode.USER_ALREADY_EXIST);
        }

        int number = createNumber();
        try {
            sendAuthCodeMailAsync(username, number).join();
        } catch (java.util.concurrent.CompletionException e) {
            throw new ServiceException(ErrorCode.EMAIL_SEND_ERROR);
        }
        String key="auth:email:"+username;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(number), Duration.ofMinutes(verificationCodeExpiryMinutes));
    }

    private int createNumber() { // 메일 코드 생성
        return new java.security.SecureRandom().nextInt(900000) + 100000; // 최소 100000인 6자리 숫자
    }

    public void checkVerificationNumber(MailVerifyRequest mailVerifyRequest) { // 이메일 코드 일치 검증
        String key="auth:email:"+mailVerifyRequest.getUsername();
        String requestCode=String.valueOf(mailVerifyRequest.getMailCode());
        String storedValue = stringRedisTemplate.opsForValue().get(key);// 인증 코드를 레디스에서 가져와야함
        if(storedValue==null){  // 저장된 인증번호가 없다
            throw new ServiceException(ErrorCode.EXPIRED_EMAIL_CODE);
        }
        if("VERIFIED".equals(storedValue))
            return;
        // 입력한 코드와 redis에 저장된 코드가 일치하지 않을 때
        if(!requestCode.equals(storedValue)){
            throw new ServiceException(ErrorCode.INVALID_EMAIL_CODE);
        }
        stringRedisTemplate.opsForValue().set(key, "VERIFIED", Duration.ofMinutes(verificationCodeExpiryMinutes));
    }
}
