package model;

import java.util.Random;

public class Board {

    private int rows;
    private int cols;
    private Cell[][] cells;
    private Theme theme;

    public Board(int rows, int cols, Theme theme) {
        this.rows = rows;
        this.cols = cols;
        this.theme = theme;
        this.cells = new Cell[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    public Theme getTheme() {
        return theme;
    }
    // Returns true if (row, col) is inside the board boundaries
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // Counts how many mines are adjacent to the given cell
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr;
                int nc = col + dc;
                if (isInBounds(nr, nc) && cells[nr][nc].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    // Recursively reveals neighbors for empty cells (0 adjacent mines)
    private void revealNeighbors(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr;
                int nc = col + dc;
                if (!isInBounds(nr, nc)) {
                    continue;
                }
                Cell neighbor = cells[nr][nc];
                // Skip already revealed or mine cells
                if (neighbor.isRevealed() || neighbor.isMine()) {
                    continue;
                }
                neighbor.setRevealed(true);
                if (neighbor.getAdjacentMines() == 0) {
                    // Continue flood fill for empty neighbors
                    revealNeighbors(nr, nc);
                }
            }
        }
    }


    public void generate(Difficulty difficulty) {
        // Number of mines to place on the board
        int minesToPlace = difficulty.getMines();
        System.out.println(">>> minesToPlace = " + minesToPlace);


        // 1) Reset all cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                cell.setMine(false);
                cell.setRevealed(false);
                cell.setFlagged(false);
                cell.setAdjacentMines(0);
                cell.setType(CellType.REGULAR);
                cell.setQuestion(null);

            }
        }

        // 2) Randomly place mines
        Random random = new Random();
        int placed = 0;
        while (placed < minesToPlace) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            Cell cell = cells[r][c];
            if (!cell.isMine()) {
                cell.setMine(true);
                placed++;
            }
        }

        // 3) Compute adjacent mines for every non-mine cell
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                if (!cell.isMine()) {
                    int count = countAdjacentMines(r, c);
                    cell.setAdjacentMines(count);
                }
            }
        }
    }
    /**
     * Randomly places question cells (CellType.QUESTION) on the board.
     * Does NOT assign a specific Question object yet – רק מסמן את סוג התא.
     */
    public void placeQuestionCells(int questionCount) {
        if (questionCount <= 0) {
            return;
        }

        Random random = new Random();
        int placed = 0;

        // הגנה: לא לשים יותר שאלות ממספר התאים
        int maxPossible = rows * cols;
        questionCount = Math.min(questionCount, maxPossible);

        while (placed < questionCount) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            Cell cell = cells[r][c];

            // לא לשים שאלה על מוקש, ולא על תא שכבר הוגדר כשאלה
            if (cell.isMine()) {
                continue;
            }
            if (cell.getType() == CellType.QUESTION) {
                continue;
            }

            cell.setType(CellType.QUESTION);
            cell.setAdjacentMines(0); // משבצת שאלה לא מציגה מספר מוקשים
            placed++;
        }

        System.out.println(">>> placed " + placed + " question cells");
    }



    /**
     * Reveals the cell at (row, col) and returns the outcome.
     * This method is a great candidate for white-box, black-box,
     * and unit tests in the assignment.
     */
    public RevealResult revealCell(int row, int col) {

        // 1) Out of bounds → ignore
        if (!isInBounds(row, col)) {
            return RevealResult.IGNORED;
        }

        Cell cell = cells[row][col];

        // 2) Already revealed or flagged → ignore
        if (cell.isRevealed() || cell.isFlagged()) {
            return RevealResult.IGNORED;
        }

        // 3) Reveal the cell
        cell.setRevealed(true);
        System.out.println("Reveal (" + row + "," + col + ") mine=" + cell.isMine()
                + " adj=" + cell.getAdjacentMines());
        if (cell.getType() == CellType.QUESTION) {
            return RevealResult.QUESTION_CELL;
        }

        // 4) Mine → hit mine
        if (cell.isMine()) {
            return RevealResult.HIT_MINE;
        }

        // 5) No adjacent mines → expand empty area
        if (cell.getAdjacentMines() == 0) {
            revealNeighbors(row, col);
            return RevealResult.EMPTY_AREA;
        }

        // 6) Safe number cell
        return RevealResult.SAFE_NUMBER;
    }


}
