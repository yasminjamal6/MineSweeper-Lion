package model;

public class EasyQuestion extends Question {

    public EasyQuestion(String text, String[] options, int correctIndex) {
        super(text, options, correctIndex, QuestionLevel.EASY);
    }
}
