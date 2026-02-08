package com.chip.board.me.presentation.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateGoalPointsRequest(
        @Min(0) long goalPoints
) {}
