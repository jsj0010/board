package com.chip.board.challenge.infrastructure.persistence.adapter;

import com.chip.board.challenge.domain.ChallengeStatus;
import com.chip.board.challenge.application.port.ChallengeSyncIndexPort;
import com.chip.board.syncproblem.application.port.dto.ChallengeSyncSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChallengeSyncIndexRedisAdapter implements ChallengeSyncIndexPort {

    private static final String KEY = "solvedac:challenge:sync:index";

    private static final String F_CHALLENGE_ID = "challengeId";
    private static final String F_STATUS = "status";
    private static final String F_PREPARE_FINALIZED = "prepareFinalized";
    private static final String F_CLOSE_FINALIZED = "closeFinalized";

    private final StringRedisTemplate redis;

    @Override
    public Optional<ChallengeSyncSnapshot> load() {
        try {
            Object cidObj = redis.opsForHash().get(KEY, F_CHALLENGE_ID);
            Object statusObj = redis.opsForHash().get(KEY, F_STATUS);
            Object pfObj = redis.opsForHash().get(KEY, F_PREPARE_FINALIZED);
            Object cfObj = redis.opsForHash().get(KEY, F_CLOSE_FINALIZED);

            if (cidObj == null || statusObj == null || pfObj == null || cfObj == null) return Optional.empty();

            long challengeId = Long.parseLong(cidObj.toString());
            ChallengeStatus status = ChallengeStatus.valueOf(statusObj.toString());
            boolean prepareFinalized = "1".equals(pfObj.toString());
            boolean closeFinalized = "1".equals(cfObj.toString());

            return Optional.of(new ChallengeSyncSnapshot(challengeId, status, prepareFinalized, closeFinalized));
        } catch (Exception e) {
            // 파싱 실패/오염 시 self-heal
            redis.delete(KEY);
            return Optional.empty();
        }
    }

    @Override
    public void save(ChallengeSyncSnapshot snapshot) {
        redis.opsForHash().putAll(KEY, Map.of(
                F_CHALLENGE_ID, Long.toString(snapshot.challengeId()),
                F_STATUS, snapshot.status().name(),
                F_PREPARE_FINALIZED, snapshot.prepareFinalized() ? "1" : "0",
                F_CLOSE_FINALIZED, snapshot.closeFinalized() ? "1" : "0"
        ));
    }

    @Override
    public void delete() {
        redis.delete(KEY);
    }
}