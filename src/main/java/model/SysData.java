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
        return List.of();
    }

    public void saveQuestions(List<Question> questions) {
    }

    /*public GameHistory loadHistory() {
        return new GameHistory();
    }

    public void saveHistory(GameHistory history) {
    }*/
}
