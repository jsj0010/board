package com.chip.board.qnaboard.presentation.swagger;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.exception.ErrorCode;
import com.chip.board.global.config.swagger.SwaggerApiFailedResponse;
import com.chip.board.global.config.swagger.SwaggerApiResponses;
import com.chip.board.global.config.swagger.SwaggerApiSuccessResponse;
import com.chip.board.qnaboard.presentation.dto.response.question.QuestionDetailResponse;
import com.chip.board.qnaboard.presentation.dto.response.question.QuestionListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag(name = "QnA Question Query", description = "QnA 질문 조회 API")
public interface QnaQuestionQuerySwagger {

    @Operation(summary = "질문 목록 조회", description = "페이지네이션으로 질문 목록을 조회합니다.")
    @Parameter(name = "page", description = "페이지(0부터)", required = false, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "integer", defaultValue = "0"))
    @Parameter(name = "size", description = "페이지 크기(1~100)", required = false, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "integer", defaultValue = "10"))
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = QuestionListResponse.class,
                    description = "질문 목록 조회 성공"
            ),
            errors = {
                    //없음
            }
    )
    ResponseEntity<ResponseBody<QuestionListResponse>> list(int page, int size);

    @Operation(summary = "질문 상세 조회", description = "질문 상세 정보를 조회합니다.")
    @Parameter(name = "id", description = "질문 ID", required = true)
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    status = HttpStatus.OK,
                    response = QuestionDetailResponse.class,
                    description = "질문 상세 조회 성공"
            ),
            errors = {
                    @SwaggerApiFailedResponse(ErrorCode.QNA_QUESTION_NOT_FOUND),
            }
    )
    ResponseEntity<ResponseBody<QuestionDetailResponse>> detail(long id);
}
