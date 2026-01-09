package com.chip.board.challenge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChallengeCreateRequest(
        @NotBlank @Size(max = 100) String title,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) { }