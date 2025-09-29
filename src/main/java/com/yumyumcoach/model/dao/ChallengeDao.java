package com.yumyumcoach.model.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.yumyumcoach.model.dto.Challenge;

public interface ChallengeDao {
    List<Challenge> findAll();

    Optional<Challenge> findById(long id);

    Challenge save(Challenge challenge);

    void delete(long id);

    void saveAll();

    Map<Long, List<String>> participants();

    void updateParticipants(long challengeId, List<String> usernames);

    void saveParticipants();

    long nextId();
}
