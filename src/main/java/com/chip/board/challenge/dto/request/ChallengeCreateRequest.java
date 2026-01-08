package com.chip.board.challenge.dto.request;

import com.chip.board.challenge.domain.ChallengeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ChallengeCreateRequest(
        @NotBlank @Size(max = 100) String title,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt
) { }