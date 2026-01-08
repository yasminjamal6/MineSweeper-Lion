package model;

public class HardBoardSetup extends BoardSetupTemplate {
    @Override
    protected int getQuestionCount(Difficulty difficulty) { return 11; }

    @Override
    protected int getSurpriseCount(Difficulty difficulty) { return difficulty.getSurpriseCells(); }
}
