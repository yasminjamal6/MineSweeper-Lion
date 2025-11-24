package main.controller;

import model.Difficulty;

public class DifficultyMapper {

    public static Difficulty toModel(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY -> Difficulty.EASY;
            case MEDIUM -> Difficulty.MEDIUM;
            case HARD -> Difficulty.HARD;
        };
    }
}
