package model;

public class Player {

    private final String id;
    private String name;
    private BoardColor boardColor;

    public Player(String id, String name, BoardColor boardColor) {
        this.id = id;
        this.name = name;
        this.boardColor = boardColor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BoardColor getBoardColor() {
        return boardColor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBoardColor(BoardColor boardColor) {
        this.boardColor = boardColor;
    }
}
