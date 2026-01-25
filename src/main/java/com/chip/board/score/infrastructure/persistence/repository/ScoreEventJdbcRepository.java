package com.chip.board.score.infrastructure.persistence.repository;

import com.chip.board.score.application.port.ScoreEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScoreEventJdbcRepository implements ScoreEventPort {

    private final JdbcTemplate jdbcTemplate;

    // 점수 후보(credited_at IS NULL)만 score_event 적재
    public int insertScoreEventsForUncredited(long challengeId, long userId) {
        String sql = """
        INSERT IGNORE INTO score_event (challenge_id, user_id, problem_id, level, points)
        SELECT ?, usp.user_id, usp.problem_id, usp.level, ts.points
        FROM user_solved_problem usp
        JOIN tier_score ts ON ts.level = usp.level
        WHERE usp.user_id = ?
          AND usp.credited_at IS NULL
          AND usp.level IS NOT NULL
        """;
        return jdbcTemplate.update(sql, challengeId, userId);
    }

    // score_event가 존재하는 문제는 credited_at 채움(uk 때문에 insert ignore된 케이스 포함)
    public int fillCreditedAtFromScoreEvent(long userId) {
        String sql = """
        UPDATE user_solved_problem usp
        JOIN score_event se
          ON se.user_id = usp.user_id
         AND se.problem_id = usp.problem_id
        SET usp.credited_at = se.created_at
        WHERE usp.user_id = ?
          AND usp.credited_at IS NULL
        """;
        return jdbcTemplate.update(sql, userId);
    }
}
