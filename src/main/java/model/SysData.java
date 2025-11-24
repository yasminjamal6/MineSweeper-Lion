package model;

import java.util.List;

public class SysData {

    private static SysData instance;

    private SysData() {
    }

    public static SysData getInstance() {
        if (instance == null) {
            instance = new SysData();
        }
        return instance;
    }

    public List<Question> loadQuestions() {
        // קריאת CSV והחזרה כ-List<Question>
        return List.of();
    }

    public void saveQuestions(List<Question> questions) {
        // כתיבה ל-CSV
    }

    /*public GameHistory loadHistory() {
        // קריאת CSV להיסטוריית משחקים
        return new GameHistory();
    }

    public void saveHistory(GameHistory history) {
        // כתיבת היסטוריה ל-CSV
    }*/
}
