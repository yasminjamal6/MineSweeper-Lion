package model;

public abstract class BoardSetupTemplate {

    // Template Method (final)
    public final void setup(Board board, Difficulty difficulty) {
        // 1) always generate mines + numbers
        board.generate(difficulty);

        // 2) then place questions (step can vary)
        int questions = getQuestionCount(difficulty);
        board.placeQuestionCells(questions);

        // 3) then place surprises (step can vary)
        int surprises = getSurpriseCount(difficulty);
        board.placeSurpriseCells(surprises);

        // Hook (optional)
        afterSetup(board, difficulty);
    }

    // steps that vary
    protected abstract int getQuestionCount(Difficulty difficulty);
    protected abstract int getSurpriseCount(Difficulty difficulty);

    // optional hook
    protected void afterSetup(Board board, Difficulty difficulty) {}
}
