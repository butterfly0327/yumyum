package com.yumyumcoach.model.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.yumyumcoach.model.dao.ChallengeDao;
import com.yumyumcoach.model.dao.JdbcChallengeDao;
import com.yumyumcoach.model.dto.Challenge;
import com.yumyumcoach.model.service.ChallengeService;

public class ChallengeServiceImpl implements ChallengeService {
    private static final ChallengeService INSTANCE = new ChallengeServiceImpl();
    private final ChallengeDao challengeDao = JdbcChallengeDao.getInstance();

    public static ChallengeService getInstance() {
        return INSTANCE;
    }

    private ChallengeServiceImpl() {
    }

    @Override
    public List<Challenge> findAll() {
        return challengeDao.findAll();
    }

    @Override
    public Challenge create(Challenge challenge) {
        if (challenge.getStartDate() == null) {
            challenge.setStartDate(LocalDate.now());
        }
        return challengeDao.save(challenge);
    }

    @Override
    public Optional<Challenge> findById(long id) {
        return challengeDao.findById(id);
    }

    @Override
    public void delete(long id) {
        challengeDao.delete(id);
    }

    @Override
    public Map<Long, List<String>> participants() {
        return challengeDao.participants();
    }

    @Override
    public void join(long challengeId, String username) {
        List<String> participants = new ArrayList<>(challengeDao.participants().getOrDefault(challengeId, new ArrayList<>()));
        if (!participants.contains(username)) {
            participants.add(username);
            challengeDao.updateParticipants(challengeId, participants);
        }
    }

    @Override
    public void removeParticipant(long challengeId, String username) {
        List<String> participants = new ArrayList<>(challengeDao.participants().getOrDefault(challengeId, new ArrayList<>()));
        if (participants.remove(username)) {
            challengeDao.updateParticipants(challengeId, participants);
        }
    }
}
