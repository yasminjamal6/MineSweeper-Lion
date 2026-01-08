package model;

public class MediumBoardSetup extends BoardSetupTemplate {
    @Override
    protected int getQuestionCount(Difficulty difficulty) { return 7; }

    @Override
    protected int getSurpriseCount(Difficulty difficulty) { return difficulty.getSurpriseCells(); }
}
