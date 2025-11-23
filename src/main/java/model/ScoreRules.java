package model;

import java.util.concurrent.ThreadLocalRandom;

public final class ScoreRules {

    public static final class ScoreChange {
        private final int pointsDelta;
        private final int livesDelta;

        private ScoreChange(int pointsDelta, int livesDelta) {
            this.pointsDelta = pointsDelta;
            this.livesDelta = livesDelta;
        }

        public static ScoreChange of(int pointsDelta, int livesDelta) {
            return new ScoreChange(pointsDelta, livesDelta);
        }

        public int getPointsDelta() {
            return pointsDelta;
        }

        public int getLivesDelta() {
            return livesDelta;
        }
    }

    private ScoreRules() {
    }

    public static ScoreChange mineRevealed() {
        return ScoreChange.of(0, -1);
    }

    public static ScoreChange correctFlagOnMine() {
        return ScoreChange.of(1, 0);
    }

    public static ScoreChange wrongFlagOnSafeCell() {
        return ScoreChange.of(-3, 0);
    }

    public static ScoreChange safeCellRevealed() {
        return ScoreChange.of(1, 0);
    }

    public static ScoreChange questionAnswered(Difficulty difficulty,
                                               QuestionLevel level,
                                               boolean correct) {
        switch (difficulty) {
            case EASY:
                return easyQuestion(level, correct);
            case MEDIUM:
                return mediumQuestion(level, correct);
            case HARD:
                return hardQuestion(level, correct);
            default:
                return ScoreChange.of(0, 0);
        }
    }

    public static ScoreChange surpriseTriggered(Difficulty difficulty,
                                                SurpriseType type) {
        int points = type == SurpriseType.GOOD
                ? difficulty.getSurpriseGoodPoints()
                : difficulty.getSurpriseBadPoints();
        int lives = type == SurpriseType.GOOD ? 1 : -1;
        return ScoreChange.of(points, lives);
    }

    public static int activationCostPoints(Difficulty difficulty) {
        return difficulty.getActivationCostPoints();
    }

    public static int initialLives(Difficulty difficulty) {
        return difficulty.getInitialLives();
    }

    private static ScoreChange easyQuestion(QuestionLevel level, boolean correct) {
        switch (level) {
            case EASY:
                if (correct) {
                    return ScoreChange.of(3, 1);
                }
                return randomOr(ScoreChange.of(-3, 0), ScoreChange.of(0, 0));
            case MEDIUM:
                if (correct) {
                    return ScoreChange.of(6, 0);
                }
                return randomOr(ScoreChange.of(-6, 0), ScoreChange.of(0, 0));
            case HARD:
                if (correct) {
                    return ScoreChange.of(10, 0);
                }
                return ScoreChange.of(-10, 0);
            case EXPERT:
                if (correct) {
                    return ScoreChange.of(15, 2);
                }
                return ScoreChange.of(-15, -1);
            default:
                return ScoreChange.of(0, 0);
        }
    }

    private static ScoreChange mediumQuestion(QuestionLevel level, boolean correct) {
        switch (level) {
            case EASY:
                if (correct) {
                    return ScoreChange.of(8, 1);
                }
                return ScoreChange.of(-8, 0);
            case MEDIUM:
                if (correct) {
                    return ScoreChange.of(10, 1);
                }
                return randomOr(ScoreChange.of(-10, -1), ScoreChange.of(0, 0));
            case HARD:
                if (correct) {
                    return ScoreChange.of(15, 1);
                }
                return ScoreChange.of(-15, -1);
            case EXPERT:
                if (correct) {
                    return ScoreChange.of(20, 2);
                }
                return ScoreChange.of(-20, -2);
            default:
                return ScoreChange.of(0, 0);
        }
    }

    private static ScoreChange hardQuestion(QuestionLevel level, boolean correct) {
        switch (level) {
            case EASY:
                if (correct) {
                    return ScoreChange.of(10, 1);
                }
                return ScoreChange.of(-10, -1);
            case MEDIUM:
                if (correct) {
                    return randomOr(
                            ScoreChange.of(15, 1),
                            ScoreChange.of(15, 2)
                    );
                }
                return randomOr(
                        ScoreChange.of(-15, -1),
                        ScoreChange.of(-15, -2)
                );
            case HARD:
                if (correct) {
                    return ScoreChange.of(20, 2);
                }
                return ScoreChange.of(-20, -2);
            case EXPERT:
                if (correct) {
                    return ScoreChange.of(40, 3);
                }
                return ScoreChange.of(-40, -3);
            default:
                return ScoreChange.of(0, 0);
        }
    }

    private static ScoreChange randomOr(ScoreChange a, ScoreChange b) {
        return ThreadLocalRandom.current().nextBoolean() ? a : b;
    }
}
