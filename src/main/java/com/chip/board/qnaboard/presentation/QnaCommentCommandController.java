package com.chip.board.qnaboard.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.global.jwt.annotation.CurrentUserId;
import com.chip.board.qnaboard.application.service.CommentFacade;
import com.chip.board.qnaboard.presentation.dto.request.comment.UpdateCommentRequest;
import com.chip.board.qnaboard.presentation.dto.request.question.CreateCommentRequest;
import com.chip.board.qnaboard.presentation.dto.response.question.IdResponse;
import com.chip.board.qnaboard.presentation.swagger.QnaCommentCommandSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
@RestController
@RequestMapping("/api/qna/questions/{questionId}/comments")
@RequiredArgsConstructor
public class QnaCommentCommandController implements QnaCommentCommandSwagger {

    private final CommentFacade commentFacade;

    @PostMapping
    public ResponseEntity<ResponseBody<IdResponse>> addComment(
            @PathVariable long questionId,
            @CurrentUserId Long userId,
            @RequestBody @Valid CreateCommentRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUtils.createSuccessResponse(
                        commentFacade.addComment(questionId, userId, req.content())
                )
        );
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ResponseBody<String>> update(
            @PathVariable long questionId,
            @PathVariable long commentId,
            @CurrentUserId Long userId,
            @RequestBody @Valid UpdateCommentRequest req
    ) {
        commentFacade.updateComment(questionId, commentId, req.content(), userId);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("댓글 수정 완료"));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseBody<String>> delete(
            @PathVariable long questionId,
            @PathVariable long commentId,
            @CurrentUserId Long userId
    ) {
        commentFacade.deleteComment(questionId, commentId, userId);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("댓글 삭제 완료"));
    }
}
