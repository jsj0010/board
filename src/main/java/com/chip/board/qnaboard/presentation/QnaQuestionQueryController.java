package com.chip.board.qnaboard.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.qnaboard.application.service.QuestionFacade;
import com.chip.board.qnaboard.presentation.dto.response.question.QuestionDetailResponse;
import com.chip.board.qnaboard.presentation.dto.response.question.QuestionListResponse;
import com.chip.board.qnaboard.presentation.swagger.QnaQuestionQuerySwagger;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/qna/questions")
public class QnaQuestionQueryController implements QnaQuestionQuerySwagger {

    private final QuestionFacade facade;

    @GetMapping
    public ResponseEntity<ResponseBody<QuestionListResponse>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(
                ResponseUtils.createSuccessResponse(facade.list(page, size))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseBody<QuestionDetailResponse>> detail(@PathVariable long id) {
        return ResponseEntity.ok(
                ResponseUtils.createSuccessResponse(facade.detail(id))
        );
    }
}