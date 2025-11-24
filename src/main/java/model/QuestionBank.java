package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class QuestionBank {

    private final Map<QuestionLevel, List<Question>> questionsByLevel;
    private final Random random;

    public QuestionBank() {
        this.questionsByLevel = new EnumMap<>(QuestionLevel.class);

        for (QuestionLevel level : QuestionLevel.values()) {
            questionsByLevel.put(level, new ArrayList<>());
        }
        this.random = new Random();
        loadFromCsv("/Questions.csv");
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
    private void loadFromCsv(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                throw new RuntimeException("CSV not found at: " + path);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            boolean skipHeader = true;

            while ((line = br.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] p = line.split(",", -1);

                String text = p[1];
                int diff = Integer.parseInt(p[2].trim());

                String[] options = new String[]{
                        p[3].trim(), // A
                        p[4].trim(), // B
                        p[5].trim(), // C
                        p[6].trim()  // D
                };

                int correctIndex = switch (p[7].trim().toUpperCase()) {
                    case "A" -> 0;
                    case "B" -> 1;
                    case "C" -> 2;
                    case "D" -> 3;
                    default -> -1;
                };

                QuestionLevel level = switch (diff) {
                    case 1 -> QuestionLevel.EASY;
                    case 2 -> QuestionLevel.MEDIUM;
                    case 3 -> QuestionLevel.HARD;
                    default -> QuestionLevel.EASY;
                };

                Question q = new Question(text, options, correctIndex, level);
                addQuestion(q);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load CSV", e);
        }
    }

}
