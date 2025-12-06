package com.chip.board.global.jwt;

import com.chip.board.register.domain.User;
import com.chip.board.register.domain.Role;

public record JwtClaims(
        Long userId,
        Role role
) {
    public static JwtClaims create(User user) {
        return new JwtClaims(user.getId(),user.getRole());
    }
}
