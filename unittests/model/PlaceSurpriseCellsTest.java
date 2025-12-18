package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaceSurpriseCellsTest {

    private int countSurprise(Board board) {
        int count = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).getType() == CellType.SURPRISE) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    public void placeSurpriseCells_zeroOrNegative_doesNothing() {
        Board board = new Board(3, 3, null);

        // initial: no surprise cells
        assertEquals(0, countSurprise(board));

        board.placeSurpriseCells(0);
        assertEquals(0, countSurprise(board));

        board.placeSurpriseCells(-5);
        assertEquals(0, countSurprise(board));
    }

    @Test
    public void placeSurpriseCells_placesExactCount_whenBoardIsEmpty() {
        Board board = new Board(4, 4, null);

        int requested = 5;
        board.placeSurpriseCells(requested);

        assertEquals(requested, countSurprise(board));

        // verify each surprise cell has adjacentMines = 0
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.getType() == CellType.SURPRISE) {
                    assertEquals(0, cell.getAdjacentMines());
                }
            }
        }
    }

    @Test
    public void placeSurpriseCells_neverPlacesOnMineOrQuestion() {
        Board board = new Board(3, 3, null);

        // block some cells
        board.getCell(0, 0).setMine(true);
        board.getCell(0, 1).setType(CellType.QUESTION);
        board.getCell(1, 1).setType(CellType.QUESTION);

        int totalCells = board.getRows() * board.getCols();
        int blocked = 3; // 1 mine + 2 questions
        int available = totalCells - blocked;

        board.placeSurpriseCells(available);

        assertEquals(available, countSurprise(board));

        // verify blocked cells are not surprise
        assertTrue(board.getCell(0, 0).isMine());
        assertNotEquals(CellType.SURPRISE, board.getCell(0, 0).getType());

        assertEquals(CellType.QUESTION, board.getCell(0, 1).getType());
        assertEquals(CellType.QUESTION, board.getCell(1, 1).getType());
    }

    @Test
    public void placeSurpriseCells_moreThanBoardSize_clampsToMaxPossible() {
        Board board = new Board(2, 2, null);

        // 2x2 => 4 cells
        board.placeSurpriseCells(999);

        assertEquals(4, countSurprise(board));

        // all cells are surprise and adjacentMines = 0
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                assertEquals(CellType.SURPRISE, cell.getType());
                assertEquals(0, cell.getAdjacentMines());
            }
        }
    }
}
