package com.yumyumcoach.model.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.yumyumcoach.model.dto.Challenge;

public interface ChallengeService {
    List<Challenge> findAll();

    Challenge create(Challenge challenge);

    Optional<Challenge> findById(long id);

    void delete(long id);

    Map<Long, List<String>> participants();

    void join(long challengeId, String username);

    void removeParticipant(long challengeId, String username);
}
