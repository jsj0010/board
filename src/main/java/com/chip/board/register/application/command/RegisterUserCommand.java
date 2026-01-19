package com.chip.board.register.application.command;

import com.chip.board.register.presentation.dto.request.UserRegisterRequest;

import java.util.Objects;

public record RegisterUserCommand(
        String username,
        String password,
        String name,
        String department,
        String studentId,
        Integer grade,
        String bojId
) {
    public static RegisterUserCommand from(UserRegisterRequest req) {
        Objects.requireNonNull(req, "req must not be null");
        return new RegisterUserCommand(
                req.getUsername(),
                req.getPassword(),
                req.getName(),
                req.getDepartment(),
                req.getStudentId(),
                req.getGrade(),
                req.getBojId()
        );
    }
}