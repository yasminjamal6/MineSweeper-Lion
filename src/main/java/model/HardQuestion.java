package model;

public class HardQuestion extends Question {

    public HardQuestion(String text, String[] options, int correctIndex) {
        super(text, options, correctIndex, QuestionLevel.HARD);
    }
}
