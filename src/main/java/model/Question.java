package model;

public class Question {

    private String text;
    private String[] options;
    private int correctIndex;
    private QuestionLevel level;

    public Question(String text, String[] options, int correctIndex, QuestionLevel level) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public QuestionLevel getLevel() {
        return level;
    }

    public boolean isCorrect(int index) {
        return index == correctIndex;
    }
}
