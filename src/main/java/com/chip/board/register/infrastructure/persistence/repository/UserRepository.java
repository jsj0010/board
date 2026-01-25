package com.chip.board.register.infrastructure.persistence.repository;

import java.util.Optional;

import com.chip.board.register.domain.User;
import com.chip.board.register.application.port.dto.ChallengeRankingRow;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    boolean existsByBojId(String bojId);

    @Query("""
    select new com.chip.board.register.application.port.dto.ChallengeRankingRow(
        u.id,
        u.name,
        u.bojId,
        u.department,
        coalesce(cur.solvedCount, 0),
        coalesce(cur.totalPoints, 0L),
        cur.lastRankNo,
        cur.currentRankNo,
        case
            when cur.lastRankNo is null or cur.currentRankNo is null then null
            else (cur.lastRankNo - cur.currentRankNo)
        end
    )
    from User u
    left join ChallengeUserResultEntity cur
        on cur.id.userId = u.id
       and cur.id.challengeId = :challengeId
    order by
        case when cur.currentRankNo is null then 1 else 0 end asc,
        cur.currentRankNo asc,
        u.id asc
""")
    Page<ChallengeRankingRow> findRankingsAllUsers(@Param("challengeId") long challengeId, Pageable pageable);

    long countByDeletedFalse();
}
