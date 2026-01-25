package com.chip.board.qnaboard.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.qnaboard.presentation.dto.request.comment.UpdateCommentRequest;
import com.chip.board.qnaboard.presentation.dto.request.question.CreateCommentRequest;
import com.chip.board.qnaboard.presentation.dto.response.question.IdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag(name = "QnA Comment", description = "QnA 댓글 생성/수정/삭제 API")
public interface QnaCommentCommandSwagger {

    @Operation(summary = "댓글 작성", description = "특정 질문(questionId)에 댓글을 작성합니다.")
    @Parameter(name = "questionId", description = "질문 ID", required = true)
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.CREATED,
                    response = IdResponse.class,
                    description = "댓글 작성 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.QNA_QUESTION_NOT_FOUND)
            }
    )
    ResponseEntity<ResponseBody<IdResponse>> addComment(long questionId, @Parameter(hidden = true) Long userId, CreateCommentRequest req);

    @Operation(summary = "댓글 수정", description = "특정 질문(questionId)의 댓글(commentId)을 수정합니다.")
    @Parameter(name = "questionId", description = "질문 ID", required = true)
    @Parameter(name = "commentId", description = "댓글 ID", required = true)
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String.class,
                    description = "댓글 수정 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.QNA_COMMENT_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.QNA_COMMENT_FORBIDDEN)
            }
    )
    ResponseEntity<ResponseBody<String>> update(long questionId, long commentId, @Parameter(hidden = true) Long userId, UpdateCommentRequest req);

    @Operation(summary = "댓글 삭제", description = "특정 질문(questionId)의 댓글(commentId)을 삭제(soft delete)합니다.")
    @Parameter(name = "questionId", description = "질문 ID", required = true)
    @Parameter(name = "commentId", description = "댓글 ID", required = true)
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String.class,
                    description = "댓글 삭제 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.QNA_COMMENT_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.QNA_COMMENT_FORBIDDEN)
            }
    )
    ResponseEntity<ResponseBody<String>> delete(long questionId, long commentId, @Parameter(hidden = true) Long userId);
}
