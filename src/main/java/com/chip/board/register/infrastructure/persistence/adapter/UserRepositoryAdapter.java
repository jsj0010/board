package com.chip.board.register.infrastructure.persistence.adapter;

import com.chip.board.register.application.port.UserRepositoryPort;
import com.chip.board.register.domain.User;
import com.chip.board.register.infrastructure.persistence.repository.UserRepository;
import com.chip.board.register.application.port.dto.ChallengeRankingRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Component
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

    @Override
    public Page<ChallengeRankingRow> findRankingsAllUsers(long challengeId, Pageable pageable) {
        return userRepository.findRankingsAllUsers(challengeId, pageable);
    }

    @Override
    public long countByDeletedFalse() {
        return userRepository.countByDeletedFalse();
    }
}
