package model;

public abstract class Question {

    private final String text;
    private final String[] options;
    private final int correctIndex;
    private final QuestionLevel level;

    protected Question(String text, String[] options, int correctIndex, QuestionLevel level) {
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
