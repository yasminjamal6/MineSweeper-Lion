package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class RevealQuestionCellTest {

    // UT1 – בדיקת יחידה של ליאן: חשיפה ראשונה של תא "שאלה"
    @Test
    public void revealQuestionCellFirstTimeReturnsSafeNumberAndRevealsCell() {
        Board board = new Board(3, 3, null);

        // נבחר תא מסוים שיתנהג אצלנו כ"תא שאלה"
        Cell question = board.getCell(1, 1);
        question.setType(CellType.QUESTION);   // אם השם אצלכם שונה – לעדכן
        question.setMine(false);
        question.setAdjacentMines(2);          // שכנים -> מספר בטוח
        question.setFlagged(false);
        question.setRevealed(false);

        // פעולה שנבדקת
        RevealResult result = board.revealCell(1, 1);

        // ציפיות הבדיקה
        assertEquals(RevealResult.SAFE_NUMBER, result);
        assertTrue(question.isRevealed());
    }
}
