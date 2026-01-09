package com.chip.board.cooldown.infra;

import java.time.LocalDateTime;

public class ApiCooldownActiveException extends RuntimeException {
    private final String apiKey;
    private final LocalDateTime until;

    public ApiCooldownActiveException(String apiKey, LocalDateTime until) {
        super("Cooldown active: " + apiKey + " until " + until);
        this.apiKey = apiKey;
        this.until = until;
    }

    public String apiKey() { return apiKey; }
    public LocalDateTime until() { return until; }
}
