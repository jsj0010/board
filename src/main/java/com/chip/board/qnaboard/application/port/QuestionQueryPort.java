package com.chip.board.qnaboard.application.port;

import com.chip.board.qnaboard.infrastructure.persistence.dto.QuestionSummaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface QuestionQueryPort {
    Page<QuestionSummaryRow> findSummaries(Pageable pageable);
    Optional<QuestionDetailView> findDetail(long questionId);
    boolean existsActiveById(long questionId);
    record QuestionDetailView(
            long id,
            String title,
            String content,
            long authorId,
            String authorName,
            boolean solved,
            java.time.LocalDateTime createdAt,
            long commentCount,
            long likeCount
    ) {}
}
