package model;

public class Cell {

    private final int row;
    private final int col;
    private boolean revealed;
    private boolean flagged;
    private CellType type;
    private int adjacentMines;
    private Question question;
    private boolean surpriseUsed;


    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.revealed = false;
        this.flagged = false;
        this.type = CellType.REGULAR;
        this.adjacentMines = 0;
        this.question = null;
        this.surpriseUsed = false;
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

    /**
     * Returns true if this cell contains a mine.
     */
    public boolean isMine() {
        return type == CellType.MINE;
    }

    /**
     * Sets or clears a mine on this cell by updating its type.
     */
    public void setMine(boolean mine) {
        this.type = mine ? CellType.MINE : CellType.REGULAR;
    }

    /**
     * Returns true if this cell contains a surprise gift.
     * (Requires a matching CellType value such as GIFT or SURPRISE.)
     */
    public boolean isGift() {
        return type == CellType.SURPRISE;  // Change if you use a different enum value name
    }

    /**
     * Returns true if this cell is a question cell with an attached question.
     */
    public boolean hasQuestion() {
        return type == CellType.QUESTION && question != null; // Adjust enum name if needed
    }

    public boolean isSurpriseUsed() {
        return surpriseUsed;
    }

    public void setSurpriseUsed(boolean surpriseUsed) {
        this.surpriseUsed = surpriseUsed;
    }
}
