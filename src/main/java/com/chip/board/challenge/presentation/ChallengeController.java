package com.chip.board.challenge.presentation;

import com.chip.board.challenge.presentation.dto.request.ChallengeCreateRequest;
import com.chip.board.challenge.presentation.dto.response.ChallengeInfoResponse;
import com.chip.board.challenge.application.ChallengeCommandService;
import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeCommandService challengeCommandService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/hold")
    public ResponseEntity<ResponseBody<ChallengeInfoResponse>> hold(@RequestBody @Valid ChallengeCreateRequest request) {
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse( challengeCommandService.hold(request)));
    }
    @GetMapping("/info")
    public ResponseEntity<ResponseBody<ChallengeInfoResponse>> info(
            @RequestParam("challengeId") Long challengeId
    ) {
        ChallengeInfoResponse data = challengeCommandService.getInfo(challengeId);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse(data));
    }

}
