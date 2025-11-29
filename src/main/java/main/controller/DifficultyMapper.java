package main.controller;

import model.Difficulty;


/**
 * Utility class for mapping difficulty levels between the Controller and Model layers.
 */
public class DifficultyMapper {

    public static Difficulty toModel(GameSetupController.Difficulty diff) {
        return switch (diff) {
            // Mapped directly using switch expression for conciseness and completeness.
            case EASY -> Difficulty.EASY;
            case MEDIUM -> Difficulty.MEDIUM;
            case HARD -> Difficulty.HARD;
        };
    }
}
