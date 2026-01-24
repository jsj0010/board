package com.chip.board.qnaboard.application.service;

import com.chip.board.qnaboard.application.component.reader.TimeAgoFormatter;
import com.chip.board.qnaboard.application.port.LikePort;
import com.chip.board.qnaboard.application.port.QuestionQueryPort;
import com.chip.board.qnaboard.infrastructure.persistence.dto.QuestionSummaryRow;
import com.chip.board.qnaboard.presentation.dto.response.comment.CommentResponse;
import com.chip.board.qnaboard.presentation.dto.response.question.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionFacade {

    private final QuestionService questionService;
    private final TimeAgoFormatter timeAgoFormatter;

    @Transactional(readOnly = true)
    public QuestionListResponse list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionSummaryRow> result = questionService.list(pageable);

        List<QuestionSummaryResponse> items = result.getContent().stream()
                .map(this::toSummary)
                .toList();

        return new QuestionListResponse(items, page, size, result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public IdResponse create(String title, String content, long userId) {
        long id = questionService.create(title, content, userId);
        return new IdResponse(id);
    }

    @Transactional(readOnly = true)
    public QuestionDetailResponse detail(long id) {
        QuestionQueryPort.QuestionDetailView detail = questionService.getDetail(id);

        List<CommentResponse> comments = questionService.listComments(id).stream()
                .map(c -> new CommentResponse(
                        c.getId(),
                        c.getAuthorId(),
                        c.getAuthorName(),
                        c.getContent(),
                        timeAgoFormatter.format(c.getCreatedAt())
                ))
                .toList();

        return new QuestionDetailResponse(
                detail.id(),
                detail.title(),
                detail.content(),
                detail.authorId(),
                detail.authorName(),
                timeAgoFormatter.format(detail.createdAt()),
                detail.solved(),
                detail.commentCount(),
                detail.likeCount(),
                comments
        );
    }

    @Transactional
    public ToggleLikeResponse toggleLike(long questionId, long userId) {
        LikePort.ToggleResult result = questionService.toggleLike(questionId, userId);
        return new ToggleLikeResponse(result.liked(), result.likeCount());
    }

    @Transactional
    public void markSolved(long questionId, boolean solved, long userId) {
        questionService.markSolved(questionId, solved, userId);
    }

    private QuestionSummaryResponse toSummary(QuestionSummaryRow row) {
        return new QuestionSummaryResponse(
                row.getId(),
                row.getTitle(),
                row.getAuthorName(),
                timeAgoFormatter.format(row.getCreatedAt()),
                row.isSolved(),
                row.getCommentCount(),
                row.getLikeCount()
        );
    }

    @Transactional
    public void update(long id, String title, String content, long requesterId) {
        questionService.update(id, title, content, requesterId);
    }

    @Transactional
    public void delete(long id, long requesterId) {
        questionService.delete(id, requesterId);
    }
}
