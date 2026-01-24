package com.chip.board.qnaboard.application.port;

public interface LikePort {
    ToggleResult toggle(long questionId, long userId);
    record ToggleResult(boolean liked, long likeCount) {}
    long countByQuestionId(long questionId);
}
