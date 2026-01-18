package com.chip.board.baselinesync.infrastructure.persistence;

import com.chip.board.baselinesync.infrastructure.persistence.dto.BaselineTarget;
import com.chip.board.baselinesync.infrastructure.persistence.dto.DeltaPageTarget;
import com.chip.board.baselinesync.infrastructure.persistence.dto.SyncTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserSolvedSyncStateJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<BaselineTarget> BASELINE_TARGET_MAPPER = (rs, rowNum) ->
            new BaselineTarget(
                    rs.getLong("user_id"),
                    rs.getString("boj_id"),
                    rs.getInt("next_page")
            );

    private static final RowMapper<SyncTarget> SYNC_TARGET_MAPPER = (rs, rowNum) ->
            new SyncTarget(
                    rs.getLong("user_id"),
                    rs.getString("boj_id")
            );

    private static final RowMapper<DeltaPageTarget> DELTA_TARGET_MAPPER = (rs, rowNum) ->
            new DeltaPageTarget(
                    rs.getLong("user_id"),
                    rs.getString("boj_id"),
                    rs.getInt("next_page"),
                    rs.getInt("observed_solved_count")
            );


    //baseline_ready=false 중 1명을 잡음
    public Optional<BaselineTarget> findBaselineTarget(long userId) {
        String sql = """
            SELECT s.user_id, u.boj_id, s.next_page
            FROM user_solved_sync_state s
            JOIN `user` u ON u.user_id = s.user_id
            WHERE s.user_id = ?
              AND s.baseline_ready = 0
              AND u.boj_id IS NOT NULL
              AND TRIM(u.boj_id) <> ''
            LIMIT 1
            """;

        List<BaselineTarget> rows = jdbcTemplate.query(sql, BASELINE_TARGET_MAPPER, userId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

   //페이지 처리 단계에서 last_solved_count업데이트 x
    public void advancePage(long userId) {
        jdbcTemplate.update("""
            UPDATE user_solved_sync_state
            SET next_page = next_page + 1
            WHERE user_id = ?
        """, userId);
    }

    public void markBaselineReady(long userId) {
        jdbcTemplate.update("""
            UPDATE user_solved_sync_state
            SET baseline_ready = 1,
                last_sync_at = NOW(6),
                next_page = 1
            WHERE user_id = ?
        """, userId);
    }

    public void initLastSolvedCountOnce(long userId, int totalCount) {
        jdbcTemplate.update("""
        UPDATE user_solved_sync_state
        SET last_solved_count = ?
        WHERE user_id = ?
          AND baseline_ready = 0
          AND next_page = 1
          AND last_solved_count = 0
    """, totalCount, userId);
    }

    public void updateObserved(long userId, int observedSolvedCount) {
        jdbcTemplate.update("""
            UPDATE user_solved_sync_state
            SET observed_solved_count = ?,
                observed_at = NOW(6)
            WHERE user_id = ?
        """, observedSolvedCount, userId);
    }

    public void finishDelta(long userId, int observedSolvedCount) {
        jdbcTemplate.update("""
        UPDATE user_solved_sync_state
        SET last_solved_count = ?,
            last_sync_at = NOW(6),
            next_page = 1
        WHERE user_id = ?
    """, observedSolvedCount, userId);
    }

    public boolean existsObservePending(LocalDateTime windowStart) {
        Boolean v = jdbcTemplate.queryForObject("""
        SELECT EXISTS(
          SELECT 1
          FROM user_solved_sync_state s
          JOIN user u ON u.user_id = s.user_id
          WHERE s.baseline_ready = 1
            AND u.boj_id IS NOT NULL
            AND TRIM(u.boj_id) <> ''
            AND (s.observed_at IS NULL OR s.observed_at < ?)
        )
    """, Boolean.class, windowStart);
        return v != null && v;
    }

    public boolean existsDeltaPending(LocalDateTime windowStart) {
        Boolean v = jdbcTemplate.queryForObject("""
        SELECT EXISTS(
          SELECT 1
          FROM user_solved_sync_state s
          JOIN user u ON u.user_id = s.user_id
          WHERE s.baseline_ready = 1
            AND u.boj_id IS NOT NULL
            AND TRIM(u.boj_id) <> ''
            AND s.observed_at IS NOT NULL
            AND s.observed_at >= ?
            AND s.last_solved_count < s.observed_solved_count
        )
    """, Boolean.class, windowStart);
        return v != null && v;
    }

    public Optional<Long> pickOneForScoring() {
        String sql = """
        SELECT s.user_id
        FROM user_solved_sync_state s
        WHERE s.baseline_ready = 1
          AND EXISTS (
            SELECT 1
            FROM user_solved_problem usp
            WHERE usp.user_id = s.user_id
              AND usp.credited_at IS NULL
          )
        ORDER BY s.user_id ASC
        LIMIT 1
        FOR UPDATE SKIP LOCKED
    """;

        var list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"));
        return list.stream().findFirst();
    }

    public boolean existsAnyScoreable() {
        Boolean v = jdbcTemplate.queryForObject("""
        SELECT EXISTS(
          SELECT 1
          FROM user_solved_sync_state s
          WHERE s.baseline_ready = 1
            AND EXISTS (
              SELECT 1
              FROM user_solved_problem usp
              WHERE usp.user_id = s.user_id
                AND usp.credited_at IS NULL
            )
        )
    """, Boolean.class);

        return v != null && v;
    }

    public List<Long> findObserveUserIds(LocalDateTime windowStart) {
        String sql = """
            SELECT s.user_id
            FROM user_solved_sync_state s
            JOIN `user` u ON u.user_id = s.user_id
            WHERE s.baseline_ready = 1
              AND (s.observed_at IS NULL OR s.observed_at < ?)
              AND u.boj_id IS NOT NULL
              AND u.boj_id <> ''
            ORDER BY s.user_id ASC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), windowStart);
    }
    public Optional<SyncTarget> findObserveTarget(long userId, LocalDateTime windowStart) {
        String sql = """
            SELECT s.user_id, u.boj_id
            FROM user_solved_sync_state s
            JOIN `user` u ON u.user_id = s.user_id
            WHERE s.user_id = ?
              AND s.baseline_ready = 1
              AND (s.observed_at IS NULL OR s.observed_at < ?)
              AND u.boj_id IS NOT NULL
              AND u.boj_id <> ''
            LIMIT 1
            """;

        List<SyncTarget> rows = jdbcTemplate.query(sql, SYNC_TARGET_MAPPER, userId, windowStart);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public Optional<DeltaPageTarget> findDeltaTarget(long userId, LocalDateTime windowStart) {
        String sql = """
            SELECT s.user_id, u.boj_id, s.next_page, s.observed_solved_count
            FROM user_solved_sync_state s
            JOIN `user` u ON u.user_id = s.user_id
            WHERE s.user_id = ?
              AND s.baseline_ready = 1
              AND s.observed_at IS NOT NULL
              AND s.observed_at >= ?
              AND (s.last_sync_at IS NULL OR s.last_sync_at < ?)
              AND s.observed_solved_count > s.last_solved_count
              AND u.boj_id IS NOT NULL
              AND u.boj_id <> ''
            LIMIT 1
            """;

        List<DeltaPageTarget> rows = jdbcTemplate.query(sql, DELTA_TARGET_MAPPER, userId, windowStart, windowStart);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}