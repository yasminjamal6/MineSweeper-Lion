package model;

public class Game {

    private final Difficulty difficulty;
    private final Board boardA;
    private final Board boardB;
    private final Player playerA;
    private final Player playerB;
    private Player currentPlayer;
    private int lives;
    private int score;
    private GameState state;

    public Game(Difficulty difficulty,
                Player playerA,
                Player playerB) {

        this.difficulty = difficulty;
        this.playerA = playerA;
        this.playerB = playerB;
        this.currentPlayer = playerA;

        this.lives = difficulty.getInitialLives();
        this.score = 0;
        this.state = GameState.NOT_STARTED;

        int rows = difficulty.getRows();
        int cols = difficulty.getCols();

        this.boardA = new Board(rows, cols, playerA.getBoardColor());
        this.boardB = new Board(rows, cols, playerB.getBoardColor());
    }

    public void startGame() {
        boardA.generate(difficulty);
        boardB.generate(difficulty);
        state = GameState.RUNNING;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Board getBoardA() {
        return boardA;
    }

    public Board getBoardB() {
        return boardB;
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public GameState getState() {
        return state;
    }

    public void switchTurn() {
        currentPlayer = currentPlayer == playerA ? playerB : playerA;
    }

    public void addScore(int delta) {
        score += delta;
    }

    public void loseLives(int delta) {
        lives -= delta;
        if (lives <= 0) {
            lives = 0;
            state = GameState.FINISHED;
        }
    }

    public void endGame() {
        state = GameState.FINISHED;
    }

    public boolean isGameOver() {
        return state == GameState.FINISHED;
    }
}
