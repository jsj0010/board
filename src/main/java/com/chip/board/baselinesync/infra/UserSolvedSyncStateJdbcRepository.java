package com.chip.board.baselinesync.infra;

import com.chip.board.baselinesync.model.BaselineTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserSolvedSyncStateJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    //baseline_ready=false 중 1명을 잡음
    public Optional<BaselineTarget> pickOneForBaseline() {
        String sql = """
        SELECT s.user_id, s.next_page, u.boj_id
        FROM user_solved_sync_state s
        JOIN user u ON u.user_id = s.user_id
        WHERE s.baseline_ready = 0
        AND u.boj_id IS NOT NULL
        AND TRIM(u.boj_id) <> ''
        ORDER BY (s.next_page > 1) DESC, s.user_id ASC
        LIMIT 1
        FOR UPDATE SKIP LOCKED
    """;

        List<BaselineTarget> list = jdbcTemplate.query(sql, (rs, rowNum) ->
                new BaselineTarget(
                        rs.getLong("user_id"),
                        rs.getString("boj_id"),
                        rs.getInt("next_page")
                )
        );
        return list.stream().findFirst();
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
}