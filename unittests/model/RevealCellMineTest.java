package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class  RevealCellMineTest {

    @Test
    public void constructorCreatesGridAndStoresTheme() {
        Theme theme = new Theme("id", "name", "preview", "cell", "revealed");
        Board board = new Board(2, 3, theme);

        assertEquals(2, board.getRows());
        assertEquals(3, board.getCols());
        assertSame(theme, board.getTheme());
        assertEquals(2, board.getCells().length);
        assertEquals(3, board.getCells()[0].length);
        Cell last = board.getCell(1, 2);
        assertEquals(1, last.getRow());
        assertEquals(2, last.getCol());
    }

    @Test
    public void generatePlacesExpectedNumberOfMinesAndResetsState() {
        Board board = new Board(Difficulty.EASY.getRows(), Difficulty.EASY.getCols(), null);

        // dirty one cell so we know generate resets it
        Cell dirty = board.getCell(0, 0);
        dirty.setMine(true);
        dirty.setFlagged(true);
        dirty.setRevealed(true);
        dirty.setAdjacentMines(3);

        board.generate(Difficulty.EASY);

        int mines = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isMine()) {
                    mines++;
                } else {
                    assertTrue(cell.getAdjacentMines() >= 0);
                }
                assertFalse(cell.isFlagged());
                assertFalse(cell.isRevealed());
            }
        }
        assertEquals(Difficulty.EASY.getMines(), mines);
    }

    @Test
    public void revealOutOfBoundsIsIgnored() {
        Board board = new Board(2, 2, null);
        RevealResult result = board.revealCell(-1, 0);

        assertEquals(RevealResult.IGNORED, result);
        assertFalse(board.getCell(0, 0).isRevealed());
    }

    @Test
    public void revealFlaggedCellIsIgnored() {
        Board board = new Board(2, 2, null);
        Cell cell = board.getCell(0, 0);
        cell.setFlagged(true);

        RevealResult result = board.revealCell(0, 0);

        assertEquals(RevealResult.IGNORED, result);
        assertFalse(cell.isRevealed());
    }

    @Test
    public void revealAlreadyRevealedCellIsIgnored() {
        Board board = new Board(2, 2, null);
        Cell cell = board.getCell(0, 0);
        cell.setRevealed(true);

        RevealResult result = board.revealCell(0, 0);

        assertEquals(RevealResult.IGNORED, result);
        assertTrue(cell.isRevealed());
    }

    @Test
    public void testRevealMineReturnsHitMine() {
        Board board = new Board(3, 3, null);

        // make (1,1) a mine and make sure it's hidden & not flagged
        Cell cell = board.getCell(1, 1);
        cell.setMine(true);
        cell.setRevealed(false);
        cell.setFlagged(false);

        RevealResult result = board.revealCell(1, 1);

        assertEquals(RevealResult.HIT_MINE, result);
    }

    @Test
    public void revealSafeNumberReturnsSafeNumber() {
        Board board = new Board(2, 2, null);
        Cell cell = board.getCell(0, 0);
        cell.setAdjacentMines(2);

        RevealResult result = board.revealCell(0, 0);

        assertEquals(RevealResult.SAFE_NUMBER, result);
        assertTrue(cell.isRevealed());
    }

    @Test
    public void revealEmptyAreaFloodFillsNeighbors() {
        Board board = new Board(3, 3, null);

        // place one mine, mark a revealed neighbor, and mix zero/number neighbors
        board.getCell(0, 0).setMine(true);
        board.getCell(0, 1).setAdjacentMines(1);
        board.getCell(0, 1).setRevealed(true);

        Cell origin = board.getCell(1, 1);
        origin.setAdjacentMines(0);

        board.getCell(1, 2).setAdjacentMines(0);
        board.getCell(2, 2).setAdjacentMines(0);
        board.getCell(2, 1).setAdjacentMines(2);

        RevealResult result = board.revealCell(1, 1);

        assertEquals(RevealResult.EMPTY_AREA, result);
        assertTrue(origin.isRevealed());
        assertTrue(board.getCell(1, 2).isRevealed());
        assertTrue(board.getCell(2, 2).isRevealed());
        assertTrue(board.getCell(2, 1).isRevealed());
        assertFalse(board.getCell(0, 0).isRevealed());
        assertTrue(board.getCell(0, 1).isRevealed());
    }
}
