package com.chip.board.oauth.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.oauth.application.service.PasswordResetService;
import com.chip.board.oauth.presentation.dto.request.PasswordMailRequest;
import com.chip.board.oauth.presentation.dto.request.PasswordResetRequest;
import com.chip.board.oauth.presentation.dto.request.PasswordVerifyRequest;
import com.chip.board.oauth.presentation.swagger.PasswordSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordController implements PasswordSwagger {

    private final PasswordResetService passwordResetService;

    @PostMapping("/mail")
    public ResponseEntity<ResponseBody<String>> mailSend(@Valid @RequestBody PasswordMailRequest req) {
        passwordResetService.sendResetCode(req.username());
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("이메일 전송 성공"));
    }

    @PostMapping("/mail/verification")
    public ResponseEntity<ResponseBody<String>> verifyMail(@Valid @RequestBody PasswordVerifyRequest req) {
        passwordResetService.verifyCode(req.username(), req.mailCode());
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("인증이 완료되었습니다."));
    }

    @PostMapping("/reset")
    public ResponseEntity<ResponseBody<String>> resetPassword(@Valid @RequestBody PasswordResetRequest req) {
        passwordResetService.resetPassword(req.username(), req.newPassword());
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("비밀번호 변경 성공"));
    }
}
