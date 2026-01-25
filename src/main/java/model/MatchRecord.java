package model;

public class MatchRecord {

    private String playedAtIso;
    private String Teammate;
    private String result;
    private int score;
    private long durationSeconds;
    private int boardSize;
    private String difficulty;

    public MatchRecord() {
    }

    public MatchRecord(String playedAtIso,
                       String Teammate,
                       String result,
                       int score,
                       long durationSeconds,
                       int boardSize,
                       String difficulty) {
        this.playedAtIso = playedAtIso;
        this.Teammate = Teammate;
        this.result = result;
        this.score = score;
        this.durationSeconds = durationSeconds;
        this.boardSize = boardSize;
        this.difficulty = difficulty;
    }

    public String getPlayedAtIso() {
        return playedAtIso;
    }

    public String getTeammate() {
        return Teammate;
    }

    public String getResult() {
        return result;
    }

    public int getScore() {
        return score;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public String getDifficulty() {
        return difficulty;
    }

}
