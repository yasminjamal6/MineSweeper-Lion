package model;

public class ExpertQuestion extends Question {

    public ExpertQuestion(String text, String[] options, int correctIndex) {
        super(text, options, correctIndex, QuestionLevel.EXPERT);
    }
}
