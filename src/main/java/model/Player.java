package model;

public class Player {

    private final String id;
    private String name;
    private Theme theme;

    public Player(String id, String name, Theme theme) {
        this.id = id;
        this.name = name;
        this.theme = theme;
    }

    public Theme getTheme() {
        return theme;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }
}
