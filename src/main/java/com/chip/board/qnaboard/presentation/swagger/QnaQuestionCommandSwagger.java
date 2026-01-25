package com.chip.board.qnaboard.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.qnaboard.presentation.dto.request.question.CreateQuestionRequest;
import com.chip.board.qnaboard.presentation.dto.request.question.UpdateQuestionRequest;
import com.chip.board.qnaboard.presentation.dto.response.question.IdResponse;
import com.chip.board.qnaboard.presentation.dto.response.question.ToggleLikeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag(name = "QnA Question", description = "QnA 질문 생성/수정/삭제/좋아요/해결처리 API")
public interface QnaQuestionCommandSwagger {

    @Operation(summary = "질문 작성", description = "QnA 질문을 작성합니다.")
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.CREATED,
                    response = IdResponse.class,
                    description = "질문 작성 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND)
            }
    )
    ResponseEntity<ResponseBody<IdResponse>> create(@Parameter(hidden = true) Long userId, CreateQuestionRequest req);

    @Operation(summary = "좋아요 토글", description = "질문 좋아요를 토글합니다(좋아요/취소).")
    @Parameter(name = "id", description = "질문 ID", required = true)
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = ToggleLikeResponse.class,
                    description = "좋아요 토글 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.QNA_QUESTION_NOT_FOUND)

            }
    )
    ResponseEntity<ResponseBody<ToggleLikeResponse>> toggleLike(long id, @Parameter(hidden = true) Long userId);

    @Operation(summary = "해결 상태 변경", description = "질문 해결 상태를 변경합니다. solved=true/false")
    @Parameter(name = "id", description = "질문 ID", required = true)
    @Parameter(name = "solved", description = "해결 여부", required = true)
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String.class,
                    description = "해결 상태 변경 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.QNA_QUESTION_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.QNA_QUESTION_FORBIDDEN)

            }
    )
    ResponseEntity<ResponseBody<String>> markSolved(long id, @Parameter(hidden = true) Long userId, boolean solved);

    @Operation(summary = "질문 수정", description = "질문 제목/내용을 수정합니다.")
    @Parameter(name = "id", description = "질문 ID", required = true)
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String.class,
                    description = "질문 수정 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.QNA_QUESTION_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.QNA_QUESTION_FORBIDDEN)
            }
    )
    ResponseEntity<ResponseBody<String>> update(long id, @Parameter(hidden = true) Long userId, UpdateQuestionRequest req);

    @Operation(summary = "질문 삭제", description = "질문을 삭제(soft delete)합니다.")
    @Parameter(name = "id", description = "질문 ID", required = true)
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = String.class,
                    description = "질문 삭제 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.QNA_QUESTION_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.USER_NOT_FOUND),
                    @SwaggerApiFailedResponse(ErrorCode.QNA_QUESTION_FORBIDDEN)
            }
    )
    ResponseEntity<ResponseBody<String>> delete(long id,@Parameter(hidden = true)  Long userId);
}
