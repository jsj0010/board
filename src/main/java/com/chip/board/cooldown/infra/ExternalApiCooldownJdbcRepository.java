package com.chip.board.cooldown.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class ExternalApiCooldownJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    /** 쿨다운 활성이면 "쿨다운 종료 시각" 반환, 아니면 null */
    public LocalDateTime findActiveCooldownUntil(String apiKey, int cooldownMinutes) {
        return jdbcTemplate.query("""
                SELECT cooldown_started_at
                FROM external_api_cooldown
                WHERE api_key = ?
                  AND cooldown_started_at + INTERVAL ? MINUTE > NOW()
                """,
                rs -> {
                    if (!rs.next()) return null;
                    var started = rs.getTimestamp(1).toLocalDateTime();
                    return started.plusMinutes(cooldownMinutes);
                },
                apiKey, cooldownMinutes
        );
    }

    /** 429 등 발생 시: NOW()로 시작 시각 갱신(업서트) */
    public void openCooldownNow(String apiKey, int status) {
        jdbcTemplate.update("""
            INSERT INTO external_api_cooldown (api_key, cooldown_started_at, status)
            VALUES (?, NOW(), ?)
            ON DUPLICATE KEY UPDATE
              cooldown_started_at = NOW(),
              status = VALUES(status)
            """, apiKey, status);
    }
}