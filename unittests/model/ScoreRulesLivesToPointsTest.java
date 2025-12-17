package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ScoreRulesLivesToPointsTest {

    @Test
    public void nullDifficulty_returnsZero() {
        assertEquals(0, ScoreRules.livesToPoints(null, 5));
    }

    @Test
    public void zeroLives_returnsZero() {
        assertEquals(0, ScoreRules.livesToPoints(Difficulty.EASY, 0));
    }

    @Test
    public void negativeLives_returnsZero() {
        assertEquals(0, ScoreRules.livesToPoints(Difficulty.EASY, -3));
    }

    @Test
    public void positiveLives_easy_returnsLivesTimesAbsActivationCost() {
        int expected = 3 * Math.abs(Difficulty.EASY.getActivationCostPoints());
        assertEquals(expected, ScoreRules.livesToPoints(Difficulty.EASY, 3));
    }

    @Test
    public void positiveLives_medium_returnsLivesTimesAbsActivationCost() {
        int expected = 2 * Math.abs(Difficulty.MEDIUM.getActivationCostPoints());
        assertEquals(expected, ScoreRules.livesToPoints(Difficulty.MEDIUM, 2));
    }
}
