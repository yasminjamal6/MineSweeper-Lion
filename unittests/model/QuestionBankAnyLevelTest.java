package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class QuestionBankAnyLevelTest {

    @Test
    public void getRandomQuestionAnyLevel_returnsQuestion_whenBankIsNotEmpty() {
        QuestionBank bank = new QuestionBank();

        assertTrue("Question bank should not be empty", bank.getTotalQuestions() > 0);

        Question q = bank.getRandomQuestionAnyLevel();
        assertNotNull("Expected a non-null question", q);
        assertNotNull("Expected question level not to be null", q.getLevel());
    }
}
