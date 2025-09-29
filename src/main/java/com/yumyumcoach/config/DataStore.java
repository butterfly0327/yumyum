package com.yumyumcoach.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.Challenge;
import com.yumyumcoach.model.dto.DietRecord;
import com.yumyumcoach.model.dto.ExerciseRecord;
import com.yumyumcoach.model.dto.FollowInfo;
import com.yumyumcoach.model.dto.FoodItem;
import com.yumyumcoach.model.dto.Post;
import com.yumyumcoach.model.dto.Profile;

public class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    private final ObjectMapper mapper;

    private Path accountsPath;
    private Path profilesPath;
    private Path dietPath;
    private Path postsPath;
    private Path followPath;
    private Path challengesPath;
    private Path exercisePath;
    private Path participantsPath;

    private final List<Account> accounts = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    private final List<DietRecord> dietRecords = Collections.synchronizedList(new ArrayList<>());
    private final List<Post> posts = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, FollowInfo> followMap = new ConcurrentHashMap<>();
    private final List<Challenge> challenges = Collections.synchronizedList(new ArrayList<>());
    private final List<ExerciseRecord> exerciseRecords = Collections.synchronizedList(new ArrayList<>());
    private final Map<Long, List<String>> challengeParticipants = new ConcurrentHashMap<>();

    private DataStore() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public synchronized void initialize(String dataDirectory) {
        Objects.requireNonNull(dataDirectory, "dataDirectory");
        Path basePath = Path.of(dataDirectory);
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        accountsPath = basePath.resolve("accounts.json");
        profilesPath = basePath.resolve("profiles.json");
        dietPath = basePath.resolve("식단데이터.json");
        postsPath = basePath.resolve("community.json");
        followPath = basePath.resolve("follow.json");
        challengesPath = basePath.resolve("챌린지데이터.json");
        exercisePath = basePath.resolve("exercise.json");
        participantsPath = basePath.resolve("participants.json");

        loadAccounts();
        loadProfiles();
        loadDiet();
        loadPosts();
        loadFollows();
        loadChallenges();
        loadExercise();
        loadParticipants();
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Map<String, Profile> getProfiles() {
        return profiles;
    }

    public List<DietRecord> getDietRecords() {
        return dietRecords;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public Map<String, FollowInfo> getFollowMap() {
        return followMap;
    }

    public List<Challenge> getChallenges() {
        return challenges;
    }

    public List<ExerciseRecord> getExerciseRecords() {
        return exerciseRecords;
    }

    public Map<Long, List<String>> getChallengeParticipants() {
        return challengeParticipants;
    }

    public synchronized void saveAccounts() {
        writeValue(accountsPath, accounts);
    }

    public synchronized void saveProfiles() {
        writeValue(profilesPath, profiles);
    }

    public synchronized void saveDiet() {
        List<Map<String, Object>> serialized = new ArrayList<>();
        for (DietRecord record : dietRecords) {
            Map<String, Object> map = new HashMap<>();
            map.put("식단ID", record.getId());
            map.put("날짜", record.getDate().toString());
            map.put("식사구분", record.getMealType());
            List<Map<String, Object>> foods = new ArrayList<>();
            for (FoodItem item : record.getFoods()) {
                Map<String, Object> foodMap = new HashMap<>();
                foodMap.put("식품코드", item.getCode());
                foodMap.put("식품명", item.getName());
                foodMap.put("에너지", item.getEnergy());
                foodMap.put("탄수화물", item.getCarbohydrate());
                foodMap.put("단백질", item.getProtein());
                foodMap.put("지방", item.getFat());
                foodMap.put("식품중량", item.getWeight());
                foods.add(foodMap);
            }
            map.put("음식", foods);
            serialized.add(map);
        }
        writeValue(dietPath, serialized);
    }

    public synchronized void savePosts() {
        writeValue(postsPath, posts);
    }

    public synchronized void saveFollows() {
        writeValue(followPath, followMap);
    }

    public synchronized void saveChallenges() {
        writeValue(challengesPath, challenges);
    }

    public synchronized void saveExercise() {
        writeValue(exercisePath, exerciseRecords);
    }

    public synchronized void saveParticipants() {
        writeValue(participantsPath, challengeParticipants);
    }

    private void loadAccounts() {
        accounts.clear();
        List<Account> loaded = readValue(accountsPath, new TypeReference<List<Account>>() {});
        if (loaded != null) {
            accounts.addAll(loaded);
        }
    }

    private void loadProfiles() {
        profiles.clear();
        Map<String, Profile> loaded = readValue(profilesPath, new TypeReference<Map<String, Profile>>() {});
        if (loaded != null) {
            profiles.putAll(loaded);
        }
    }

    private void loadDiet() {
        dietRecords.clear();
        List<Map<String, Object>> raw = readValue(dietPath, new TypeReference<List<Map<String, Object>>>() {});
        if (raw == null) {
            return;
        }
        for (Map<String, Object> entry : raw) {
            DietRecord record = new DietRecord();
            record.setId(((Number) entry.getOrDefault("식단ID", System.nanoTime())).longValue());
            Object dateValue = entry.get("날짜");
            if (dateValue != null) {
                record.setDate(LocalDate.parse(String.valueOf(dateValue)));
            }
            record.setMealType(String.valueOf(entry.getOrDefault("식사구분", "기타")));
            List<Map<String, Object>> foods = (List<Map<String, Object>>) entry.get("음식");
            List<FoodItem> items = new ArrayList<>();
            if (foods != null) {
                for (Map<String, Object> food : foods) {
                    FoodItem item = new FoodItem();
                    item.setCode(String.valueOf(food.getOrDefault("식품코드", "")));
                    item.setName(String.valueOf(food.getOrDefault("식품명", "")));
                    item.setEnergy(parseDouble(food.get("에너지")));
                    item.setCarbohydrate(parseDouble(food.get("탄수화물")));
                    item.setProtein(parseDouble(food.get("단백질")));
                    item.setFat(parseDouble(food.get("지방")));
                    item.setWeight(String.valueOf(food.getOrDefault("식품중량", "")));
                    items.add(item);
                }
            }
            record.setFoods(items);
            dietRecords.add(record);
        }
    }

    private void loadPosts() {
        posts.clear();
        List<Post> loaded = readValue(postsPath, new TypeReference<List<Post>>() {});
        if (loaded != null) {
            posts.addAll(loaded);
        }
    }

    private void loadFollows() {
        followMap.clear();
        Map<String, FollowInfo> loaded = readValue(followPath, new TypeReference<Map<String, FollowInfo>>() {});
        if (loaded != null) {
            followMap.putAll(loaded);
        }
    }

    private void loadChallenges() {
        challenges.clear();
        List<Challenge> loaded = readValue(challengesPath, new TypeReference<List<Challenge>>() {});
        if (loaded != null) {
            challenges.addAll(loaded);
        }
    }

    private void loadExercise() {
        exerciseRecords.clear();
        List<ExerciseRecord> loaded = readValue(exercisePath, new TypeReference<List<ExerciseRecord>>() {});
        if (loaded != null) {
            exerciseRecords.addAll(loaded);
        }
    }

    private void loadParticipants() {
        challengeParticipants.clear();
        Map<Long, List<String>> loaded = readValue(participantsPath, new TypeReference<Map<Long, List<String>>>() {});
        if (loaded != null) {
            challengeParticipants.putAll(loaded);
        }
    }

    private double parseDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private <T> T readValue(Path path, TypeReference<T> type) {
        if (path == null || !Files.exists(path)) {
            return null;
        }
        try {
            return mapper.readValue(path.toFile(), type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeValue(Path path, Object value) {
        if (path == null) {
            return;
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<Account> findAccount(String username) {
        if (username == null) {
            return Optional.empty();
        }
        synchronized (accounts) {
            return accounts.stream().filter(a -> username.equals(a.getUsername())).findFirst();
        }
    }

    public Optional<Profile> findProfile(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(profiles.get(username));
    }
}
