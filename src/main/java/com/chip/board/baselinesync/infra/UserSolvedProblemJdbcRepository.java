package com.chip.board.baselinesync.infra;

import com.chip.board.baselinesync.model.SolvedProblemItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserSolvedProblemJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void upsertBatch(long userId, List<SolvedProblemItem> items) {
        if (items == null || items.isEmpty()) return;

        String sql = """
            INSERT INTO user_solved_problem (user_id, problem_id, level)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                level = VALUES(level)
        """;

        jdbcTemplate.batchUpdate(sql, items, 500, (ps, item) -> {
            ps.setLong(1, userId);
            ps.setInt(2, item.problemId());
            ps.setObject(3, item.level()); // null 허용
        });
    }
}
