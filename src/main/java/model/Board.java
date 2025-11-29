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

    public void generate(Difficulty difficulty) {

    }
}
