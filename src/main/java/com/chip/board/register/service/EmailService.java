package com.chip.board.register.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

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
    public void sendMail(String email, int number) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject(AUTH_CODE_EMAIL_SUBJECT);
            String body = String.format(AUTH_CODE_EMAIL_BODY, number);
            message.setText(body,"UTF-8", "html");
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new ServiceException(ErrorCode.EMAIL_SEND_ERROR);
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
}
