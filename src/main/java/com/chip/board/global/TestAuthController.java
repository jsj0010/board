package com.chip.board.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestAuthController {

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal Long userId) {
        log.info("userId = {}", userId);
        return "userId = " + userId;
    }
}
