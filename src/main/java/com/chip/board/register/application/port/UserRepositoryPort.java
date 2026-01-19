package com.chip.board.register.application.port;

import com.chip.board.register.domain.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    boolean existsByBojId(String bojId);

    User save(User user);
}