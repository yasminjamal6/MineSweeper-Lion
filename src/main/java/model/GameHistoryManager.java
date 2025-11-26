package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameHistoryManager {

    private static final List<GameHistory> history = new ArrayList<>();

    public static void addGame(GameHistory game) {
        if (game != null) {
            history.add(game);
        }
    }

    public static List<GameHistory> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public static void clear() {
        history.clear();
    }
}
