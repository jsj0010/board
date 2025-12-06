package com.chip.board.register.controller;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.register.dto.request.MailRequest;
import com.chip.board.register.dto.request.MailVerifyRequest;
import com.chip.board.register.dto.request.UserRegisterRequest;
import com.chip.board.register.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/register")
public class RegisterController {
    private final RegisterService registerService;

    @GetMapping()
    public ResponseEntity<ResponseBody<List<String>>> showRegisterForm(){
        List<String> department=registerService.showRegisterForm();
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(department));
    }

    @PostMapping()
    public ResponseEntity<ResponseBody<String>> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest){
        registerService.register(userRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtils.createSuccessResponse("회원가입 성공"));
    }

    //키 생성 + 숫자 코드 저장
    @PostMapping("/mail")
    public ResponseEntity<ResponseBody<String>> mailSend(@Valid @RequestBody MailRequest mailRequest) {
        registerService.sendMail(mailRequest.getUsername());
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("이메일 전송 성공"));
    }

    //숫자 코드 검증 → "VERIFIED"로 상태 전환
    @PostMapping("/mail/verification")
    public ResponseEntity<ResponseBody<String>> verifyMail(@Valid @RequestBody MailVerifyRequest mailVerifyRequest) {
        registerService.checkVerificationNumber(mailVerifyRequest);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("인증이 완료되었습니다."));
    }
}

