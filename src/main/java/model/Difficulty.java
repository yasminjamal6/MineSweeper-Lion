package model;

public enum Difficulty {
    EASY(9, 9, 10, 6, 2, 10, 4, 8, -8),
    MEDIUM(13, 13, 26, 7, 3, 8, 8, 12, -12),
    HARD(16, 16, 44, 11, 4, 6, 12, 16, -16);

    private final int rows;
    private final int cols;
    private final int mines;
    private final int questionCells;
    private final int surpriseCells;
    private final int initialLives;
    private final int activationCostPoints;
    private final int surpriseGoodPoints;
    private final int surpriseBadPoints;

    Difficulty(int rows,
               int cols,
               int mines,
               int questionCells,
               int surpriseCells,
               int initialLives,
               int activationCostPoints,
               int surpriseGoodPoints,
               int surpriseBadPoints) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.questionCells = questionCells;
        this.surpriseCells = surpriseCells;
        this.initialLives = initialLives;
        this.activationCostPoints = activationCostPoints;
        this.surpriseGoodPoints = surpriseGoodPoints;
        this.surpriseBadPoints = surpriseBadPoints;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMines() {
        return mines;
    }

    public int getQuestionCells() {
        return questionCells;
    }

    public int getSurpriseCells() {
        return surpriseCells;
    }

    public int getInitialLives() {
        return initialLives;
    }

    public int getActivationCostPoints() {
        return activationCostPoints;
    }

    public int getSurpriseGoodPoints() {
        return surpriseGoodPoints;
    }

    public int getSurpriseBadPoints() {
        return surpriseBadPoints;
    }
}
