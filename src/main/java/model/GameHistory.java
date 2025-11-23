package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class GameHistory {

    private final String playerAName;
    private final String playerBName;
    private final Difficulty difficulty;
    private final int score;
    private final int sharedLives;
    private final boolean success;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;

    public GameHistory(String playerAName,
                       String playerBName,
                       Difficulty difficulty,
                       int score,
                       int sharedLives,
                       boolean success,
                       LocalDateTime startedAt,
                       LocalDateTime endedAt) {
        this.playerAName = playerAName;
        this.playerBName = playerBName;
        this.difficulty = difficulty;
        this.score = score;
        this.sharedLives = sharedLives;
        this.success = success;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public String getPlayerAName() {
        return playerAName;
    }

    public String getPlayerBName() {
        return playerBName;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getScore() {
        return score;
    }

    public int getSharedLives() {
        return sharedLives;
    }

    public boolean isSuccess() {
        return success;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public Duration getDuration() {
        if (startedAt == null || endedAt == null) {
            return Duration.ZERO;
        }
        return Duration.between(startedAt, endedAt);
    }
}
