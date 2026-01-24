package com.chip.board.qnaboard.infrastructure.persistence.repository;

import com.chip.board.qnaboard.domain.Question;
import com.chip.board.qnaboard.infrastructure.persistence.dto.QuestionSummaryRow;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {

    @Query("""
        select
            q.id as id,
            q.title as title,
            q.authorName as authorName,
            q.createdAt as createdAt,
            q.solved as solved,
            (select count(c.id) from QuestionComment c where c.questionId = q.id and c.deleted = false) as commentCount,
            (select count(l.id) from QuestionLike l where l.questionId = q.id) as likeCount
        from Question q
        where q.deleted = false
        order by q.createdAt desc
    """)
    Page<QuestionSummaryRow> findSummaries(Pageable pageable);

    @Query("""
        select q
        from Question q
        where q.id = :id and q.deleted = false
    """)
    Optional<Question> findActiveById(@Param("id") long id);

    boolean existsByIdAndDeletedFalse(long id);
}

