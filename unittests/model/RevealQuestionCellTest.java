package model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RevealQuestionCellTest {

    private Board board;
    private Cell questionCell;

    @Before
    public void setUpQuestionCell() {
        board = new Board(3, 3, null);
        questionCell = board.getCell(1, 1);

        // לשמור שזה באמת תא QUESTION
        questionCell.setType(CellType.QUESTION);

        questionCell.setAdjacentMines(2);
        questionCell.setFlagged(false);
        questionCell.setRevealed(false);
    }

    @Test
    public void revealQuestionCellFirstTimeReturnsQuestionCell() {
        RevealResult result = board.revealCell(1, 1);
        assertEquals(RevealResult.QUESTION_CELL, result);
    }

    @Test
    public void revealQuestionCellFirstTimeMarksCellAsRevealed() {
        board.revealCell(1, 1);
        assertTrue(questionCell.isRevealed());
    }
}
