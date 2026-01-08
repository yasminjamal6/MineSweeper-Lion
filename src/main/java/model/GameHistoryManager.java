package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameHistoryManager {

    private static final List<GameHistory> history = new ArrayList<>();
    private static final Path HISTORY_PATH = Paths.get("data", "game-history.csv");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final CopyOnWriteArrayList<GameHistoryObserver> observers = new CopyOnWriteArrayList<>();

    private static final String HEADER =
            "playerA,playerB,difficulty,score,sharedLives,success,startedAt,endedAt,playerAHeartsLeft,playerBHeartsLeft";

    static {
        loadFromFile();
    }

    public static void addGame(GameHistory game) {
        if (game != null) {
            history.add(game);
            saveToFile();
            notifyObservers();
        }
    }

    public static void addObserver(GameHistoryObserver o) {
        if (o != null) observers.addIfAbsent(o);
    }

    public static void removeObserver(GameHistoryObserver o) {
        observers.remove(o);
    }

    private static void notifyObservers() {
        System.out.println(">>> [Observer] notifyObservers called. observers=" + observers.size());

        for (GameHistoryObserver o : observers) {
            try {
                o.onHistoryChanged();
            } catch (Exception ignored) {
            }
        }
    }

    public static List<GameHistory> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public static void clear() {
        history.clear();
        saveToFile();
        notifyObservers();
    }

    private static void loadFromFile() {
        try {
            if (!Files.exists(HISTORY_PATH)) {
                ensureFileExists();
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(HISTORY_PATH, StandardCharsets.UTF_8)) {
                String line;
                boolean first = true;
                while ((line = reader.readLine()) != null) {
                    if (first) { // header
                        first = false;
                        continue;
                    }
                    if (line.isBlank()) {
                        continue;
                    }
                    String[] cols = parseCsvLine(line);
                    if (cols.length < 8) {
                        continue;
                    }
                    String playerA = cols[0];
                    String playerB = cols[1];
                    Difficulty difficulty = Difficulty.valueOf(cols[2]);
                    int score = Integer.parseInt(cols[3]);
                    int sharedLives = Integer.parseInt(cols[4]);
                    boolean success = Boolean.parseBoolean(cols[5]);
                    LocalDateTime startedAt = parseDate(cols[6]);
                    LocalDateTime endedAt = parseDate(cols[7]);
                    int playerAHeartsLeft = -1;
                    int playerBHeartsLeft = -1;
                    if (cols.length >= 10) {
                        playerAHeartsLeft = parseIntOrDefault(cols[8], -1);
                        playerBHeartsLeft = parseIntOrDefault(cols[9], -1);
                    }

                    history.add(new GameHistory(
                            playerA,
                            playerB,
                            difficulty,
                            score,
                            sharedLives,
                            success,
                            startedAt,
                            endedAt,
                            playerAHeartsLeft,
                            playerBHeartsLeft
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveToFile() {
        try {
            ensureFileExists();
            try (BufferedWriter writer = Files.newBufferedWriter(HISTORY_PATH, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (GameHistory g : history) {
                    writer.write(toCsvLine(g));
                    writer.newLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ensureFileExists() throws Exception {
        if (Files.notExists(HISTORY_PATH.getParent())) {
            Files.createDirectories(HISTORY_PATH.getParent());
        }
        if (Files.notExists(HISTORY_PATH)) {
            Files.createFile(HISTORY_PATH);
            try (BufferedWriter writer = Files.newBufferedWriter(HISTORY_PATH, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
            }
        }
    }

    private static String toCsvLine(GameHistory g) {
        return String.join(",",
                escape(g.getPlayerAName()),
                escape(g.getPlayerBName()),
                escape(g.getDifficulty() != null ? g.getDifficulty().name() : ""),
                String.valueOf(g.getScore()),
                String.valueOf(g.getSharedLives()),
                String.valueOf(g.isSuccess()),
                formatDate(g.getStartedAt()),
                formatDate(g.getEndedAt()),
                formatHearts(g.getPlayerAHeartsLeft()),
                formatHearts(g.getPlayerBHeartsLeft())
        );
    }

    private static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return FORMATTER.format(dateTime);
    }

    private static String escape(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    private static String[] parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    cols.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(ch);
                }
            }
        }
        cols.add(current.toString());
        return cols.toArray(new String[0]);
    }

    private static String formatHearts(int hearts) {
        return hearts >= 0 ? String.valueOf(hearts) : "";
    }

    private static int parseIntOrDefault(String text, int defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(text);
    }

    private static LocalDateTime parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(text, FORMATTER);
    }

}
