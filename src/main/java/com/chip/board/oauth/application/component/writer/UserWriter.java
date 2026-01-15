package com.chip.board.oauth.application.component.writer;

import com.chip.board.register.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserWriter {
    public void onLoginSuccess(User user) {
        user.onLoginSuccess();
    }
}
