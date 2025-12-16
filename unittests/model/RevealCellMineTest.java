package model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RevealCellMineTest {

    private Theme theme;
    private Board themedBoard;

    @Before
    public void setUpThemedBoard() {
        theme = new Theme("id", "name", "preview", "cell", "revealed");
        themedBoard = new Board(2, 3, theme);
    }

    @Test
    public void constructorSetsRows() {
        assertEquals(2, themedBoard.getRows());
    }

    @Test
    public void constructorSetsCols() {
        assertEquals(3, themedBoard.getCols());
    }

    @Test
    public void constructorStoresThemeReference() {
        assertSame(theme, themedBoard.getTheme());
    }

    @Test
    public void constructorInitializesGridRowCount() {
        assertEquals(2, themedBoard.getCells().length);
    }

    @Test
    public void constructorInitializesGridColumnCount() {
        assertEquals(3, themedBoard.getCells()[0].length);
    }

    @Test
    public void constructorSetsCellRowCoordinate() {
        Cell last = themedBoard.getCell(1, 2);

        assertEquals(1, last.getRow());
    }

    @Test
    public void constructorSetsCellColumnCoordinate() {
        Cell last = themedBoard.getCell(1, 2);

        assertEquals(2, last.getCol());
    }

    private Board generateDirtyEasyBoard() {
        Board board = new Board(Difficulty.EASY.getRows(), Difficulty.EASY.getCols(), null);
        Cell dirty = board.getCell(0, 0);
        dirty.setMine(true);
        dirty.setFlagged(true);
        dirty.setRevealed(true);
        dirty.setAdjacentMines(3);

        board.generate(Difficulty.EASY);
        return board;
    }

    @Test
    public void generatePlacesExpectedNumberOfMines() {
        Board board = generateDirtyEasyBoard();

        int mines = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isMine()) {
                    mines++;
                }
            }
        }

        assertEquals(Difficulty.EASY.getMines(), mines);
    }

    @Test
    public void generateResetsAllFlags() {
        Board board = generateDirtyEasyBoard();

        boolean anyFlagged = false;
        for (int r = 0; r < board.getRows() && !anyFlagged; r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isFlagged()) {
                    anyFlagged = true;
                    break;
                }
            }
        }

        assertFalse(anyFlagged);
    }

    @Test
    public void generateResetsAllRevealStates() {
        Board board = generateDirtyEasyBoard();

        boolean anyRevealed = false;
        for (int r = 0; r < board.getRows() && !anyRevealed; r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isRevealed()) {
                    anyRevealed = true;
                    break;
                }
            }
        }

        assertFalse(anyRevealed);
    }

    @Test
    public void generateEnsuresNonMinesHaveValidAdjacentCounts() {
        Board board = generateDirtyEasyBoard();

        boolean anyNegativeAdjacent = false;
        for (int r = 0; r < board.getRows() && !anyNegativeAdjacent; r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isMine() && cell.getAdjacentMines() < 0) {
                    anyNegativeAdjacent = true;
                    break;
                }
            }
        }

        assertFalse(anyNegativeAdjacent);
    }

    @Test
    public void revealOutOfBoundsReturnsIgnored() {
        Board board = new Board(2, 2, null);
        RevealResult result = board.revealCell(-1, 0);

        assertEquals(RevealResult.IGNORED, result);
    }

    @Test
    public void revealOutOfBoundsDoesNotChangeCells() {
        Board board = new Board(2, 2, null);
        board.revealCell(-1, 0);

        assertFalse(board.getCell(0, 0).isRevealed());
    }

    @Test
    public void revealFlaggedCellReturnsIgnored() {
        Board board = new Board(2, 2, null);
        Cell cell = board.getCell(0, 0);
        cell.setFlagged(true);

        RevealResult result = board.revealCell(0, 0);

        assertEquals(RevealResult.IGNORED, result);
    }

    @Test
    public void revealFlaggedCellLeavesCellHidden() {
        Board board = new Board(2, 2, null);
        Cell cell = board.getCell(0, 0);
        cell.setFlagged(true);

        board.revealCell(0, 0);

        assertFalse(cell.isRevealed());
    }

    @Test
    public void revealAlreadyRevealedCellReturnsIgnored() {
        Board board = new Board(2, 2, null);
        Cell cell = board.getCell(0, 0);
        cell.setRevealed(true);

        RevealResult result = board.revealCell(0, 0);

        assertEquals(RevealResult.IGNORED, result);
    }

    @Test
    public void revealAlreadyRevealedCellStaysRevealed() {
        Board board = new Board(2, 2, null);
        Cell cell = board.getCell(0, 0);
        cell.setRevealed(true);

        board.revealCell(0, 0);

        assertTrue(cell.isRevealed());
    }

    @Test
    public void testRevealMineReturnsHitMine() {
        Board board = new Board(3, 3, null);

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
    }

    @Test
    public void revealSafeNumberRevealsCell() {
        Board board = new Board(2, 2, null);
        Cell cell = board.getCell(0, 0);
        cell.setAdjacentMines(2);

        board.revealCell(0, 0);

        assertTrue(cell.isRevealed());
    }

    private static class FloodFillContext {
        Board board;
        Cell origin;
        RevealResult result;
    }

    private FloodFillContext createFloodFillContext() {
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

        FloodFillContext context = new FloodFillContext();
        context.board = board;
        context.origin = origin;
        context.result = result;
        return context;
    }

    @Test
    public void revealEmptyAreaReturnsEmptyArea() {
        FloodFillContext context = createFloodFillContext();

        assertEquals(RevealResult.EMPTY_AREA, context.result);
    }

    @Test
    public void revealEmptyAreaRevealsOriginCell() {
        FloodFillContext context = createFloodFillContext();

        assertTrue(context.origin.isRevealed());
    }

    @Test
    public void revealEmptyAreaRevealsZeroNeighborToRight() {
        FloodFillContext context = createFloodFillContext();

        assertTrue(context.board.getCell(1, 2).isRevealed());
    }

    @Test
    public void revealEmptyAreaRevealsZeroNeighborDiagonally() {
        FloodFillContext context = createFloodFillContext();

        assertTrue(context.board.getCell(2, 2).isRevealed());
    }

    @Test
    public void revealEmptyAreaRevealsNumberedNeighbor() {
        FloodFillContext context = createFloodFillContext();

        assertTrue(context.board.getCell(2, 1).isRevealed());
    }

    @Test
    public void revealEmptyAreaDoesNotRevealMine() {
        FloodFillContext context = createFloodFillContext();

        assertFalse(context.board.getCell(0, 0).isRevealed());
    }

    @Test
    public void revealEmptyAreaLeavesAlreadyRevealedNeighborVisible() {
        FloodFillContext context = createFloodFillContext();

        assertTrue(context.board.getCell(0, 1).isRevealed());
    }
}
