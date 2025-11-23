package model;

public class Cell {

    private final int row;
    private final int col;
    private boolean revealed;
    private boolean flagged;
    private CellType type;
    private int adjacentMines;
    private Question question;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.revealed = false;
        this.flagged = false;
        this.type = CellType.REGULAR;
        this.adjacentMines = 0;
        this.question = null;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public CellType getType() {
        return type;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public Question getQuestion() {
        return question;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
