package com.chip.board.qnaboard.presentation;

import com.chip.board.global.base.dto.ResponseBody;
import com.chip.board.global.base.dto.ResponseUtils;
import com.chip.board.global.jwt.annotation.CurrentUserId;
import com.chip.board.qnaboard.application.service.QuestionFacade;
import com.chip.board.qnaboard.presentation.dto.request.question.CreateQuestionRequest;
import com.chip.board.qnaboard.presentation.dto.request.question.UpdateQuestionRequest;
import com.chip.board.qnaboard.presentation.dto.response.question.IdResponse;
import com.chip.board.qnaboard.presentation.dto.response.question.ToggleLikeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/qna/questions")
@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
public class QnaQuestionCommandController {

    private final QuestionFacade facade;

    @PostMapping
    public ResponseEntity<ResponseBody<IdResponse>> create(
            @CurrentUserId Long userId,
            @RequestBody @Valid CreateQuestionRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUtils.createSuccessResponse(
                        facade.create(req.title(), req.content(), userId)
                )
        );
    }

    @PostMapping("/{id}/likes/toggle")
    public ResponseEntity<ResponseBody<ToggleLikeResponse>> toggleLike(
            @PathVariable long id,
            @CurrentUserId Long userId
    ) {
        return ResponseEntity.ok(
                ResponseUtils.createSuccessResponse(
                        facade.toggleLike(id, userId)
                )
        );
    }

    @PostMapping("/{id}/solve")
    public ResponseEntity<ResponseBody<String>> markSolved(
            @PathVariable long id,
            @CurrentUserId Long userId,
            @RequestParam boolean solved
    ) {
        facade.markSolved(id, solved, userId);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("처리 완료"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseBody<String>> update(
            @PathVariable long id,
            @CurrentUserId Long userId,
            @RequestBody @Valid UpdateQuestionRequest req
    ) {
        facade.update(id, req.title(), req.content(), userId);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("업데이트 완료"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseBody<String>> delete(
            @PathVariable long id,
            @CurrentUserId Long userId
    ) {
        facade.delete(id, userId);
        return ResponseEntity.ok(ResponseUtils.createSuccessResponse("삭제 완료"));
    }
}