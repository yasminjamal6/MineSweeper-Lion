package model;

import java.util.Random;

public class Board {

    private int rows;
    private int cols;
    private Cell[][] cells;
    private BoardColor boardColor;

    public Board(int rows, int cols, BoardColor boardColor) {
        this.rows = rows;
        this.cols = cols;
        this.boardColor = boardColor;
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

    public BoardColor getBoardColor() {
        return boardColor;
    }

    public void generate(Difficulty difficulty) {

    }
}
