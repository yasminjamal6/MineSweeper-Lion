package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerProfileManager {

    private static final Path PROFILE_PATH = Paths.get("data", "player-profiles.json");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, PlayerProfile> PROFILES = new LinkedHashMap<>();

    static {
        loadFromFile();
        syncProfilesWithHistory();
    }

    public static List<PlayerProfile> getProfiles() {
        syncProfilesWithHistory();
        return new ArrayList<>(PROFILES.values());
    }

    public static PlayerProfile getOrCreateProfile(String playerName, String avatarId) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }
        String key = playerName.trim();
        PlayerProfile profile = PROFILES.get(key);
        if (profile == null) {
            profile = new PlayerProfile(key, avatarId);
            PROFILES.put(key, profile);
        } else if (avatarId != null && !avatarId.isBlank()) {
            profile.setAvatarId(avatarId);
        }
        return profile;
    }

    public static void addMatchForPlayers(String playerA, String avatarA,
                                          String playerB, String avatarB,
                                          boolean success, int score,
                                          LocalDateTime startedAt, LocalDateTime endedAt,
                                          int boardSize, Difficulty difficulty) {
        String result = success ? "Win" : "Loss";
        long durationSeconds = 0;
        if (startedAt != null && endedAt != null) {
            durationSeconds = Duration.between(startedAt, endedAt).getSeconds();
        }
        String playedAtIso = endedAt != null ? FORMATTER.format(endedAt) : "";
        String diff = difficulty != null ? difficulty.name() : "";

        PlayerProfile profileA = getOrCreateProfile(playerA, avatarA);
        if (profileA != null) {
            addMatchIfMissing(profileA, new MatchRecord(
                    playedAtIso,
                    playerB,
                    result,
                    score,
                    durationSeconds,
                    boardSize,
                    diff
            ));
        }

        PlayerProfile profileB = getOrCreateProfile(playerB, avatarB);
        if (profileB != null) {
            addMatchIfMissing(profileB, new MatchRecord(
                    playedAtIso,
                    playerA,
                    result,
                    score,
                    durationSeconds,
                    boardSize,
                    diff
            ));
        }

        saveToFile();
    }

    private static void syncProfilesWithHistory() {
        try {
            List<GameHistory> history = GameHistoryManager.getHistory();
            Map<String, PlayerProfile> existing = new LinkedHashMap<>(PROFILES);
            PROFILES.clear();
            for (GameHistory record : history) {
                if (record == null) continue;
                String playerA = record.getPlayerAName();
                String playerB = record.getPlayerBName();
                String playedAtIso = record.getEndedAt() != null ? FORMATTER.format(record.getEndedAt()) : "";
                String result = record.isSuccess() ? "Win" : "Loss";
                long durationSeconds = record.getDuration().getSeconds();
                int score = record.getScore();
                int boardSize = 0;
                String diff = record.getDifficulty() != null ? record.getDifficulty().name() : "";

                PlayerProfile profileA = ensureProfileFromHistory(playerA, Avatar.SIMBA.id, existing);
                if (profileA != null) {
                    addMatchIfMissing(profileA, new MatchRecord(
                            playedAtIso,
                            playerB,
                            result,
                            score,
                            durationSeconds,
                            boardSize,
                            diff
                    ));
                }

                PlayerProfile profileB = ensureProfileFromHistory(playerB, Avatar.NALA.id, existing);
                if (profileB != null) {
                    addMatchIfMissing(profileB, new MatchRecord(
                            playedAtIso,
                            playerA,
                            result,
                            score,
                            durationSeconds,
                            boardSize,
                            diff
                    ));
                }
            }
            saveToFile();
        } catch (Exception ignored) {
        }
    }

    private static PlayerProfile ensureProfileFromHistory(String playerName,
                                                         String defaultAvatarId,
                                                         Map<String, PlayerProfile> existing) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }
        String key = playerName.trim();
        PlayerProfile profile = PROFILES.get(key);
        if (profile != null) {
            return profile;
        }
        profile = existing.get(key);
        if (profile == null) {
            profile = new PlayerProfile(key, defaultAvatarId);
        } else {
            profile.getMatches().clear();
            if (profile.getAvatarId() == null || profile.getAvatarId().isBlank()) {
                profile.setAvatarId(defaultAvatarId);
            }
        }
        PROFILES.put(key, profile);
        return profile;
    }

    private static void addMatchIfMissing(PlayerProfile profile, MatchRecord record) {
        if (profile == null || record == null) {
            return;
        }
        String key = matchKey(record);
        for (MatchRecord existing : profile.getMatches()) {
            if (matchKey(existing).equals(key)) {
                return;
            }
        }
        profile.addMatch(record);
    }

    private static String matchKey(MatchRecord record) {
        return safe(record.getPlayedAtIso()) + "|" +
                safe(record.getOpponent()) + "|" +
                safe(record.getResult()) + "|" +
                record.getScore() + "|" +
                safe(record.getDifficulty());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void loadFromFile() {
        try {
            if (!Files.exists(PROFILE_PATH)) {
                Files.createDirectories(PROFILE_PATH.getParent());
                saveToFile();
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(PROFILE_PATH, StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<PlayerProfile>>() {}.getType();
                List<PlayerProfile> profiles = GSON.fromJson(reader, listType);
                if (profiles != null) {
                    for (PlayerProfile profile : profiles) {
                        if (profile != null && profile.getPlayerName() != null) {
                            PROFILES.put(profile.getPlayerName(), profile);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void saveToFile() {
        try {
            Files.createDirectories(PROFILE_PATH.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(PROFILE_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(new ArrayList<>(PROFILES.values()), writer);
            }
        } catch (Exception ignored) {
        }
    }
}
