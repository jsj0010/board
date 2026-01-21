package com.chip.board.register.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.register.application.command.RegisterUserCommand;
import com.chip.board.register.application.command.VerifyEmailCommand;
import com.chip.board.register.application.service.BaekjoonHandleValidationService;
import com.chip.board.register.application.service.EmailUseCase;
import com.chip.board.register.application.service.RegisterUseCase;
import com.chip.board.register.domain.Department;
import com.chip.board.register.presentation.dto.request.MailRequest;
import com.chip.board.register.presentation.dto.request.MailVerifyRequest;
import com.chip.board.register.presentation.dto.request.UserRegisterRequest;
import com.chip.board.register.presentation.dto.request.ValidateBaekjoonHandleRequest;
import com.chip.board.register.presentation.dto.response.ValidateBaekjoonHandleResponse;
import com.chip.board.register.presentation.swagger.RegisterSwagger;
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
@RequestMapping("/api/register")
public class RegisterController implements RegisterSwagger {
    private final RegisterUseCase registerUseCase;
    private final EmailUseCase emailUseCase;
    private final BaekjoonHandleValidationService baekjoonHandleValidationService;

    @GetMapping
    public ResponseEntity<ResponseBody<List<String>>> showRegisterForm(){
        return ResponseEntity.ok(
                ResponseUtils.createSuccessResponse(Department.showDepartment())
        );
    }

    @PostMapping
    public ResponseEntity<ResponseBody<String>> register(@Valid @RequestBody UserRegisterRequest req) {
        RegisterUserCommand cmd = RegisterUserCommand.from(req);
        registerUseCase.register(cmd);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.createSuccessResponse("회원가입 성공"));
    }

    @PostMapping("/mail")
    public ResponseEntity<ResponseBody<String>> mailSend(@Valid @RequestBody MailRequest req) {
        emailUseCase.sendAuthCode(req.getUsername());
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("이메일 전송 성공"));
    }

    @PostMapping("/mail/verification")
    public ResponseEntity<ResponseBody<String>> verifyMail(@Valid @RequestBody MailVerifyRequest req) {
        VerifyEmailCommand cmd = new VerifyEmailCommand(req.getUsername(), req.getMailCode());
        emailUseCase.verifyCode(cmd);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("인증이 완료되었습니다."));
    }

    @PostMapping("/baekjoon/validate")
    public ResponseEntity<ResponseBody<ValidateBaekjoonHandleResponse>> validate(
            @RequestBody @Valid ValidateBaekjoonHandleRequest req
    ) {
        boolean valid = baekjoonHandleValidationService.validate(req.handle());
        ValidateBaekjoonHandleResponse response = new ValidateBaekjoonHandleResponse(valid);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(response));
    }
}
