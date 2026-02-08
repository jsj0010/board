package com.chip.board.me.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.global.jwt.annotation.CurrentUserId;
import com.chip.board.me.application.service.UserGoalService;
import com.chip.board.me.presentation.dto.request.UpdateGoalPointsRequest;
import com.chip.board.me.presentation.swagger.UserInfoSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserInfoController implements UserInfoSwagger {
    private final UserGoalService userGoalService;

    @PatchMapping("/goal-points")
    public ResponseEntity<ResponseBody<Void>> updateGoalPoints(
            @CurrentUserId Long userId,
            @Valid @RequestBody UpdateGoalPointsRequest req
    ) {
        userGoalService.updateMyGoalPoints(userId, req.goalPoints());
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(null));
    }
}
