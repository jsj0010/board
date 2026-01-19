package com.chip.board.register.infrastructure.persistence.adapter;

import com.chip.board.register.application.port.UserRepositoryPort;
import com.chip.board.register.domain.User;
import com.chip.board.register.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public boolean existsByBojId(String bojId) {
        return userRepository.existsByBojId(bojId);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
