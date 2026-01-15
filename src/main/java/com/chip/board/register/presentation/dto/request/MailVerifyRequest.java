package com.chip.board.register.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class MailVerifyRequest {

    @NotBlank
    @Pattern(regexp = ".+@kumoh.ac.kr$", message = "이메일 형식이 올바르지 않습니다.")
    private String username;

    @NotNull
    private Integer mailCode;
}
