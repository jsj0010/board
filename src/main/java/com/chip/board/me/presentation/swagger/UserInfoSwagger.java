package com.chip.board.me.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.me.presentation.dto.request.UpdateGoalPointsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;

@Tag(name = "Me User", description = "내 사용자 정보(목표점수) API")
public interface UserInfoSwagger {

    @Operation(
            summary = "내 목표점수 변경",
            description = """
                    로그인한 사용자의 목표 점수를 변경합니다.
                    - goalPoints는 0 이상이어야 합니다.
                    """
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = Void.class,
                    description = "내 목표점수 변경 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.INVALID_GOAL_POINTS),
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND)
            }
    )
    @PatchMapping("/goal-points")
    ResponseEntity<ResponseBody<Void>> updateGoalPoints(
            @Parameter(hidden = true)
            Long userId,

            @Valid @RequestBody UpdateGoalPointsRequest req
    );
}