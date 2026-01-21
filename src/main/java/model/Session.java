package model;


public class Session {
    private static String activePlayerName = "Player";

    public static void setActivePlayerName(String name) {
        if (name == null) return;
        String n = name.trim();
        if (!n.isEmpty()) activePlayerName = n;
    }

    public static String getActivePlayerName() {
        return activePlayerName;
    }
}
