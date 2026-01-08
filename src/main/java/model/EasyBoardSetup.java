package model;

public class EasyBoardSetup extends BoardSetupTemplate {
    @Override
    protected int getQuestionCount(Difficulty difficulty) { return 6; }

    @Override
    protected int getSurpriseCount(Difficulty difficulty) { return difficulty.getSurpriseCells(); }
}
