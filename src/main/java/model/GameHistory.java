package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class GameHistory {

    private final String playerAName;
    private final String playerBName;
    private final Difficulty difficulty;
    private final int score;
    private final int sharedLives;
    private final int playerAHeartsLeft;
    private final int playerBHeartsLeft;
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
        this(playerAName,
                playerBName,
                difficulty,
                score,
                sharedLives,
                success,
                startedAt,
                endedAt,
                -1,
                -1);
    }

    public GameHistory(String playerAName,
                       String playerBName,
                       Difficulty difficulty,
                       int score,
                       int sharedLives,
                       boolean success,
                       LocalDateTime startedAt,
                       LocalDateTime endedAt,
                       int playerAHeartsLeft,
                       int playerBHeartsLeft) {
        this.playerAName = playerAName;
        this.playerBName = playerBName;
        this.difficulty = difficulty;
        this.score = score;
        this.sharedLives = sharedLives;
        this.success = success;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.playerAHeartsLeft = playerAHeartsLeft;
        this.playerBHeartsLeft = playerBHeartsLeft;
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

    public int getPlayerAHeartsLeft() {
        return playerAHeartsLeft;
    }

    public int getPlayerBHeartsLeft() {
        return playerBHeartsLeft;
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

    // duration in minutes for the table
    public long getDurationMinutes() {
        return getDuration().toMinutes();
    }

    public String getResult() {
        return success ? "Win" : "Loss";
    }
    // difficulty as String (Easy / Medium / Hard)
    public String getDifficultyString() {
        return (difficulty == null) ? "" : difficulty.name();
    }

    public String getHeartsDisplay() {
        boolean aKnown = playerAHeartsLeft >= 0;
        boolean bKnown = playerBHeartsLeft >= 0;
        int fallback = difficulty != null ? difficulty.getInitialLives() : -1;
        String fallbackText = fallback >= 0 ? String.valueOf(fallback) : "N/A";

        if (!aKnown && !bKnown) {
            return fallbackText;
        }
        if (aKnown && bKnown) {
            if (playerAHeartsLeft == playerBHeartsLeft) {
                return String.valueOf(playerAHeartsLeft);
            }
            return playerAHeartsLeft + " / " + playerBHeartsLeft;
        }
        if (aKnown) {
            return playerAHeartsLeft + " / " + fallbackText;
        }
        return fallbackText + " / " + playerBHeartsLeft;
    }
}
