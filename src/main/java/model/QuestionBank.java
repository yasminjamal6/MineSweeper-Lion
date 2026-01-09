package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * QuestionBank:
 * 1. פעם ראשונה – מעתיק /Questions.csv מתוך resources ל־data/Questions.csv
 * 2. תמיד טוען ושומר את השאלות ל־data/Questions.csv
 *
 * פורמט ה-CSV:
 *   0: id
 *   1: Question text
 *   2: Difficulty (1/2/3)
 *   3: Answer A
 *   4: Answer B
 *   5: Answer C
 *   6: Answer D
 *   7: Correct letter (A/B/C/D)
 */
public class QuestionBank {

    private static final String RESOURCE_CSV = "/Questions.csv";
    private static final String DATA_CSV_PATH = "data/Questions.csv";

    private final Map<QuestionLevel, List<Question>> questionsByLevel =
            new EnumMap<>(QuestionLevel.class);
    private final Random random = new Random();
    private final QuestionFactory questionFactory = new QuestionFactory();

    private String headerLine = "id,Question,Difficulty,A,B,C,D,Correct";
    private final java.util.List<Question> allQuestions = new java.util.ArrayList<>();

    public Question getRandomQuestionAnyLevel() {
        List<Question> all = getAllQuestions();
        if (all.isEmpty()) return null;
        return all.get(random.nextInt(all.size()));
    }

    public QuestionBank() {
        for (QuestionLevel level : QuestionLevel.values()) {
            questionsByLevel.put(level, new ArrayList<>());
        }

        ensureDataCsvExists();
        loadFromDataCsv();
    }

    public void addQuestion(Question question) {
        if (question == null || question.getLevel() == null) return;
        questionsByLevel.get(question.getLevel()).add(question);
        saveToDataCsv();
    }

    public void addQuestions(List<Question> questions) {
        if (questions == null) return;
        for (Question q : questions) {
            if (q != null && q.getLevel() != null) {
                questionsByLevel.get(q.getLevel()).add(q);
            }
        }
        saveToDataCsv();
    }

    public void removeQuestion(Question question) {
        if (question == null) return;

        boolean removed = false;
        for (QuestionLevel lvl : QuestionLevel.values()) {
            List<Question> list = questionsByLevel.get(lvl);
            if (list != null && list.remove(question)) {
                removed = true;
                break;
            }
        }
        if (removed) {
            saveToDataCsv();
        }
    }

    public void updateQuestion(Question oldQ, Question newQ) {
        if (oldQ == null || newQ == null || newQ.getLevel() == null) return;

        QuestionLevel newLevel = newQ.getLevel();
        QuestionLevel foundLevel = null;
        int foundIndex = -1;

        for (QuestionLevel lvl : QuestionLevel.values()) {
            List<Question> list = questionsByLevel.get(lvl);
            if (list == null) continue;

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == oldQ) {
                    foundLevel = lvl;
                    foundIndex = i;
                    break;
                }
            }
            if (foundIndex != -1) break;
        }

        if (foundIndex == -1) {
            questionsByLevel.get(newLevel).add(newQ);
        } else {
            if (foundLevel == newLevel) {
                questionsByLevel.get(foundLevel).set(foundIndex, newQ);
            } else {
                questionsByLevel.get(foundLevel).remove(foundIndex);
                questionsByLevel.get(newLevel).add(newQ);
            }
        }

        saveToDataCsv();
    }


    public Question getRandomQuestion(QuestionLevel level) {
        List<Question> list = questionsByLevel.get(level);
        if (list == null || list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
    }

    public List<Question> getQuestionsByLevel(QuestionLevel level) {
        List<Question> list = questionsByLevel.get(level);
        if (list == null) return Collections.emptyList();
        return Collections.unmodifiableList(list);
    }

    public List<Question> getAllQuestions() {
        List<Question> all = new ArrayList<>();
        for (List<Question> list : questionsByLevel.values()) {
            all.addAll(list);
        }
        return all;
    }

    public int getTotalQuestions() {
        int total = 0;
        for (List<Question> list : questionsByLevel.values()) {
            total += list.size();
        }
        return total;
    }

    private void ensureDataCsvExists() {
        try {
            Path dataPath = Paths.get(DATA_CSV_PATH);
            if (Files.exists(dataPath)) {
                return;
            }

            if (dataPath.getParent() != null) {
                Files.createDirectories(dataPath.getParent());
            }

            try (InputStream is = getClass().getResourceAsStream(RESOURCE_CSV)) {
                if (is == null) {
                    throw new RuntimeException("Resource " + RESOURCE_CSV + " not found in classpath");
                }
                Files.copy(is, dataPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("📥 Copied " + RESOURCE_CSV + " to " + dataPath.toAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to ensure data CSV exists", e);
        }
    }

    /* ============================
       ===  LOAD & SAVE data/Questions.csv  ===
       ============================ */

    private void loadFromDataCsv() {
        for (List<Question> list : questionsByLevel.values()) {
            list.clear();
        }

        Path dataPath = Paths.get(DATA_CSV_PATH);

        try (BufferedReader br = Files.newBufferedReader(dataPath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    headerLine = line;
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) continue;

                List<String> fields = parseCsvLine(line);
                if (fields.size() < 8) {
                    System.out.println("⚠ Bad CSV line (expected 8 cols): " + line);
                    continue;
                }

                String[] p = normalizeFields(fields);

                String text = p[1].trim();

                int diff;
                try {
                    diff = Integer.parseInt(p[2].trim());
                } catch (NumberFormatException ex) {
                    System.out.println("⚠ Bad difficulty in line: " + line);
                    continue;
                }

                String[] options = new String[]{
                        p[3].trim(),
                        p[4].trim(),
                        p[5].trim(),
                        p[6].trim()
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

                Question q = questionFactory.createQuestion(level, text, options, correctIndex);
                questionsByLevel.get(level).add(q);
            }

            System.out.println("✅ Loaded " + getTotalQuestions() +
                    " questions from " + dataPath.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load data CSV", e);
        }
    }

    private void saveToDataCsv() {
        Path dataPath = Paths.get(DATA_CSV_PATH);

        try {
            if (dataPath.getParent() != null) {
                Files.createDirectories(dataPath.getParent());
            }

            try (BufferedWriter bw = Files.newBufferedWriter(dataPath, StandardCharsets.UTF_8)) {

                if (headerLine != null && !headerLine.isBlank()) {
                    bw.write(headerLine);
                } else {
                    bw.write("id,Question,Difficulty,A,B,C,D,Correct");
                }
                bw.newLine();

                int id = 1;

                for (QuestionLevel lvl : QuestionLevel.values()) {
                    List<Question> list = questionsByLevel.get(lvl);
                    if (list == null) continue;

                    for (Question q : list) {
                        String[] opts = q.getOptions();

                        int diff = switch (q.getLevel()) {
                            case EASY -> 1;
                            case MEDIUM -> 2;
                            case HARD -> 3;
                            default -> 1;
                        };

                        String correctLetter = switch (q.getCorrectIndex()) {
                            case 0 -> "A";
                            case 1 -> "B";
                            case 2 -> "C";
                            case 3 -> "D";
                            default -> "";
                        };

                        String col0 = String.valueOf(id);
                        String col1 = q.getText().replace("\n", " ").replace("\r", " ").trim();
                        String col2 = String.valueOf(diff);
                        String col3 = opts.length > 0 ? opts[0] : "";
                        String col4 = opts.length > 1 ? opts[1] : "";
                        String col5 = opts.length > 2 ? opts[2] : "";
                        String col6 = opts.length > 3 ? opts[3] : "";
                        String col7 = correctLetter;

                        String csvLine = String.join(",",
                                escapeCsv(col0),
                                escapeCsv(col1),
                                escapeCsv(col2),
                                escapeCsv(col3.replace("\n", " ").replace("\r", " ").trim()),
                                escapeCsv(col4.replace("\n", " ").replace("\r", " ").trim()),
                                escapeCsv(col5.replace("\n", " ").replace("\r", " ").trim()),
                                escapeCsv(col6.replace("\n", " ").replace("\r", " ").trim()),
                                escapeCsv(col7)
                        );
                        bw.write(csvLine);
                        bw.newLine();
                        id++;
                    }
                }
            }

            System.out.println("💾 Saved " + getTotalQuestions() +
                    " questions to " + dataPath.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        boolean needsQuoting = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    private static String[] normalizeFields(List<String> fields) {
        if (fields.size() == 8) {
            return fields.toArray(new String[0]);
        }
        if (fields.size() > 8) {
            int lastIndex = fields.size() - 1;
            String id = fields.get(0);
            String correct = fields.get(lastIndex);
            String d = fields.get(lastIndex - 1);
            String c = fields.get(lastIndex - 2);
            String b = fields.get(lastIndex - 3);
            String a = fields.get(lastIndex - 4);
            String diff = fields.get(lastIndex - 5);
            String question = String.join(",", fields.subList(1, lastIndex - 5));
            return new String[]{id, question, diff, a, b, c, d, correct};
        }
        return fields.toArray(new String[0]);
    }
}
