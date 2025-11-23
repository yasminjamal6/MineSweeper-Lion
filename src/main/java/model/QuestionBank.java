package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QuestionBank {

    private final Map<QuestionLevel, List<Question>> questionsByLevel;
    private final Random random;

    public QuestionBank() {
        this.questionsByLevel = new EnumMap<>(QuestionLevel.class);
        for (QuestionLevel level : QuestionLevel.values()) {
            questionsByLevel.put(level, new ArrayList<>());
        }
        this.random = new Random();
    }

    public void addQuestion(Question question) {
        if (question == null || question.getLevel() == null) {
            return;
        }
        questionsByLevel.get(question.getLevel()).add(question);
    }

    public void addQuestions(List<Question> questions) {
        if (questions == null) {
            return;
        }
        for (Question q : questions) {
            addQuestion(q);
        }
    }

    public Question getRandomQuestion(QuestionLevel level) {
        List<Question> list = questionsByLevel.get(level);
        if (list == null || list.isEmpty()) {
            return null;
        }
        int index = random.nextInt(list.size());
        return list.get(index);
    }

    public List<Question> getQuestionsByLevel(QuestionLevel level) {
        List<Question> list = questionsByLevel.get(level);
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    public int getTotalQuestions() {
        int total = 0;
        for (List<Question> list : questionsByLevel.values()) {
            total += list.size();
        }
        return total;
    }
}
