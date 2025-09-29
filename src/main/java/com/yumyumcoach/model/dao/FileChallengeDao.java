package com.yumyumcoach.model.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import com.yumyumcoach.config.DataStore;
import com.yumyumcoach.model.dto.Challenge;

public class FileChallengeDao implements ChallengeDao {
    private static final FileChallengeDao INSTANCE = new FileChallengeDao();
    private final DataStore store = DataStore.getInstance();
    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    public static FileChallengeDao getInstance() {
        return INSTANCE;
    }

    private FileChallengeDao() {
        long max = store.getChallenges().stream().mapToLong(Challenge::getId).max().orElse(System.currentTimeMillis());
        sequence.set(max + 1);
    }

    @Override
    public List<Challenge> findAll() {
        return store.getChallenges();
    }

    @Override
    public Optional<Challenge> findById(long id) {
        return store.getChallenges().stream().filter(challenge -> challenge.getId() == id).findFirst();
    }

    @Override
    public Challenge save(Challenge challenge) {
        if (challenge.getId() == 0L) {
            challenge.setId(nextId());
            store.getChallenges().add(challenge);
        } else {
            findById(challenge.getId()).ifPresent(existing -> {
                existing.setTitle(challenge.getTitle());
                existing.setType(challenge.getType());
                existing.setTarget(challenge.getTarget());
                existing.setDuration(challenge.getDuration());
                existing.setDescription(challenge.getDescription());
                existing.setStartDate(challenge.getStartDate());
            });
        }
        store.saveChallenges();
        return challenge;
    }

    @Override
    public void delete(long id) {
        store.getChallenges().removeIf(challenge -> challenge.getId() == id);
        store.saveChallenges();
        store.getChallengeParticipants().remove(id);
        store.saveParticipants();
    }

    @Override
    public void saveAll() {
        store.saveChallenges();
    }

    @Override
    public Map<Long, List<String>> participants() {
        return store.getChallengeParticipants();
    }

    @Override
    public void updateParticipants(long challengeId, List<String> usernames) {
        store.getChallengeParticipants().put(challengeId, new ArrayList<>(usernames));
        store.saveParticipants();
    }

    @Override
    public void saveParticipants() {
        store.saveParticipants();
    }

    @Override
    public long nextId() {
        return sequence.getAndIncrement();
    }
}
