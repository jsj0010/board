package com.chip.board.qnaboard.application.service;

import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.base.exception.ServiceException;
import com.chip.board.qnaboard.application.port.CommentPort;
import com.chip.board.qnaboard.application.port.QuestionQueryPort;
import com.chip.board.qnaboard.domain.QuestionComment;
import com.chip.board.register.application.port.UserRepositoryPort;
import com.chip.board.register.domain.Role;
import com.chip.board.register.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentPort commentPort;
    private final QuestionQueryPort questionQueryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Transactional
    public long addComment(long questionId, long userId, String content) {
        validateQuestionExists(questionId);

        User user = getUserOrThrow(userId);
        QuestionComment saved = commentPort.save(
                new QuestionComment(questionId, userId, user.getName(), content)
        );
        return saved.getId();
    }

    @Transactional
    public void updateComment(long questionId, long commentId, String content, long requesterId) {
        QuestionComment comment = findAndValidateComment(questionId, commentId, requesterId);

        comment.changeContent(content); // dirty checking
    }

    @Transactional
    public void deleteComment(long questionId, long commentId, long requesterId) {
        QuestionComment comment = findAndValidateComment(questionId, commentId, requesterId);

        comment.softDelete(); // dirty checking
    }

    private QuestionComment findAndValidateComment(long questionId, long commentId, long requesterId){
        QuestionComment comment = commentPort.findActiveById(commentId)
                .orElseThrow(() -> new ServiceException(ErrorCode.QNA_COMMENT_NOT_FOUND));

        if (!comment.getQuestionId().equals(questionId)) {
            throw new ServiceException(ErrorCode.QNA_COMMENT_NOT_FOUND);
        }

        validateOwnerOrAdmin(comment.getAuthorId(), requesterId);
        return comment;
    }

    private void validateQuestionExists(long questionId) {
        if (!questionQueryPort.existsActiveById(questionId)) {
            throw new ServiceException(ErrorCode.QNA_QUESTION_NOT_FOUND);
        }
    }

    private User getUserOrThrow(long userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateOwnerOrAdmin(long authorId, long requesterId) {
        if (authorId == requesterId) return;

        User requester = getUserOrThrow(requesterId);
        if (requester.getRole() == Role.ADMIN) return;

        throw new ServiceException(ErrorCode.QNA_COMMENT_FORBIDDEN);
    }
}
