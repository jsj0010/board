package com.chip.board.qnaboard.infrastructure.adapter;

import com.chip.board.qnaboard.application.port.QuestionQueryPort;
import com.chip.board.qnaboard.infrastructure.persistence.dto.QuestionSummaryRow;
import com.chip.board.qnaboard.infrastructure.persistence.repository.CommentJpaRepository;
import com.chip.board.qnaboard.infrastructure.persistence.repository.LikeJpaRepository;
import com.chip.board.qnaboard.infrastructure.persistence.repository.QuestionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuestionQueryAdapter implements QuestionQueryPort {

    private final QuestionJpaRepository questionJpaRepository;
    private final CommentJpaRepository commentJpaRepository;
    private final LikeJpaRepository likeJpaRepository;

    @Override
    public Page<QuestionSummaryRow> findSummaries(Pageable pageable) {
        return questionJpaRepository.findSummaries(pageable);
    }

    @Override
    public Optional<QuestionDetailView> findDetail(long questionId) {
        return questionJpaRepository.findActiveById(questionId).map(q ->
                new QuestionDetailView(
                        q.getId(),
                        q.getTitle(),
                        q.getContent(),
                        q.getAuthorId(),
                        q.getAuthorName(),
                        q.isSolved(),
                        q.getCreatedAt(),
                        commentJpaRepository.countByQuestionIdAndDeletedFalse(q.getId()),
                        likeJpaRepository.countByQuestionId(q.getId())
                )
        );
    }

    @Override
    public boolean existsActiveById(long questionId) {
        return questionJpaRepository.existsByIdAndDeletedFalse(questionId);
    }
}

