package com.chip.board.qnaboard.infrastructure.adapter;

import com.chip.board.qnaboard.application.port.LikePort;
import com.chip.board.qnaboard.domain.QuestionLike;
import com.chip.board.qnaboard.infrastructure.persistence.repository.LikeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikeAdapter implements LikePort {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    @Transactional
    public ToggleResult toggle(long questionId, long userId) {
        boolean liked;
        Optional<QuestionLike> existing = likeJpaRepository.findByQuestionIdAndUserId(questionId, userId);

        if (existing.isPresent()) {
            likeJpaRepository.delete(existing.get());
            liked = false;
        } else {
            likeJpaRepository.save(new QuestionLike(questionId, userId));
            liked = true;
        }

        long likeCount = likeJpaRepository.countByQuestionId(questionId);
        return new ToggleResult(liked, likeCount);
    }

    @Override
    public long countByQuestionId(long questionId) {
        return likeJpaRepository.countByQuestionId(questionId);
    }
}

