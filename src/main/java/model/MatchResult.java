package model;

import java.time.LocalDateTime;

public class MatchResult {

    private final LocalDateTime playedAt;
    private final Difficulty difficulty;
    private final int totalPoints;
    private final int remainingLives;

    public MatchResult(LocalDateTime playedAt,
                       Difficulty difficulty,
                       int totalPoints,
                       int remainingLives) {
        this.playedAt = playedAt;
        this.difficulty = difficulty;
        this.totalPoints = totalPoints;
        this.remainingLives = remainingLives;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getRemainingLives() {
        return remainingLives;
    }
}
