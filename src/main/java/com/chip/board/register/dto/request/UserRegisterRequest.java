package com.chip.board.register.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserRegisterRequest {

    @NotNull
    @Pattern(message = "올바르지 않은 아이디 형식입니다.",
            regexp = "^[A-Za-z0-9._%+-]+@kumoh\\.ac\\.kr$")
    private String username;

    @NotNull
    @Pattern(message = "비밀번호는 최소 10자 이상~20자 이하, 영문 대문자, 소문자, 특수문자를 포함해야 합니다.",
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@!#$%^&*()_+?])[A-Za-z\\d@!#$%^&*()_+?]{10,20}$")
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String department;

    @NotBlank
    @Pattern(message = "학번은 8자리 혹은 10자리 숫자여야 합니다.",
            regexp = "^(\\d{8}|\\d{10})$")
    private String studentId;

    @NotNull
    private Integer grade;

    @NotBlank
    private String bojId;

    @NotBlank
    @Pattern(message = "전화번호 형식이 올바르지 않습니다.",
            regexp = "^\\d{3}-\\d{4}-\\d{4}$")
    private String phoneNumber;
}