package model;

public class QuestionFactory {

    public Question createQuestion(QuestionLevel level, String text, String[] options, int correctIndex) {
        QuestionLevel resolvedLevel = level != null ? level : QuestionLevel.EASY;
        switch (resolvedLevel) {
            case EASY:
                return new EasyQuestion(text, options, correctIndex);
            case MEDIUM:
                return new MediumQuestion(text, options, correctIndex);
            case HARD:
                return new HardQuestion(text, options, correctIndex);
            default:
                return new EasyQuestion(text, options, correctIndex);
        }
    }
}
