package model;

public class MediumQuestion extends Question {

    public MediumQuestion(String text, String[] options, int correctIndex) {
        super(text, options, correctIndex, QuestionLevel.MEDIUM);
    }
}
