package com.chip.board.register.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValidateBaekjoonHandleRequest(
        @NotBlank String handle
) {}