package com.chip.board.qnaboard.infrastructure.adapter;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.qnaboard.application.port.QuestionCommandPort;
import com.chip.board.qnaboard.domain.Question;
import com.chip.board.qnaboard.infrastructure.persistence.repository.QuestionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor

public class QuestionCommandAdapter implements QuestionCommandPort {

    private final QuestionJpaRepository questionJpaRepository;

    @Override
    @Transactional
    public void update(long questionId, String title, String content) {
        Question question = questionJpaRepository.findActiveById(questionId)
                .orElseThrow(() -> new ServiceException(ErrorCode.QNA_QUESTION_NOT_FOUND));
        question.change(title, content);
        // dirty checking
    }

    @Override
    @Transactional
    public void softDelete(long questionId) {
        Question question = questionJpaRepository.findActiveById(questionId)
                .orElseThrow(() -> new ServiceException(ErrorCode.QNA_QUESTION_NOT_FOUND));
        question.softDelete();
    }

    @Override
    public Question save(Question question) {
        return questionJpaRepository.save(question);
    }

    @Override
    public void markSolved(long questionId, boolean solved) {
        Question question = questionJpaRepository.findActiveById(questionId)
                .orElseThrow(() -> new ServiceException(ErrorCode.QNA_QUESTION_NOT_FOUND));
        question.markSolved(solved);
        // dirty checking
    }
}
