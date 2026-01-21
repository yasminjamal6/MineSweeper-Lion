package model;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProfileStore {

    private static Path dir() throws IOException {
        Path dir = Paths.get(System.getProperty("user.home"), ".lion-minesweeper");
        Files.createDirectories(dir);
        return dir;
    }

    private static Path fileForPlayer(String playerName) throws IOException {
        String safe = (playerName == null ? "player" : playerName.trim().toLowerCase())
                .replaceAll("[^a-z0-9_-]", "_");
        return dir().resolve("profile_" + safe + ".txt");
    }

    public static PlayerProfile loadOrCreate(String playerName) {
        try {
            Path path = fileForPlayer(playerName);
            if (!Files.exists(path)) {
                PlayerProfile p = new PlayerProfile(playerName);
                save(p);
                return p;
            }

            PlayerProfile p = new PlayerProfile(playerName);

            for (String line : Files.readAllLines(path)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "coins" -> p.setCoins(parseIntSafe(value));
                    case "ownedAvatars" -> p.getOwnedAvatars().addAll(parseSet(value));
                    case "selectedAvatarId" -> p.selectAvatar(value); // יבחר רק אם קיים
                }
            }

            // אם במקרה הקובץ היה בלי DEFAULT - נוודא שיש
            p.addAvatar("DEFAULT");
            if (p.getSelectedAvatarId() == null || !p.ownsAvatar(p.getSelectedAvatarId())) {
                p.selectAvatar("DEFAULT");
            }

            p.ensureDefaults();
            return p;

        } catch (Exception e) {
            // fallback
            return new PlayerProfile(playerName);
        }
    }

    public static void save(PlayerProfile profile) {
        if (profile == null) return;

        try {
            Path path = fileForPlayer(profile.getPlayerName());

            String owned = String.join(",", profile.getOwnedAvatars());
            String selected = profile.getSelectedAvatarId() == null ? "DEFAULT" : profile.getSelectedAvatarId();

            String content =
                    "# Lion Minesweeper Profile\n" +
                            "coins=" + profile.getCoins() + "\n" +
                            "ownedAvatars=" + owned + "\n" +
                            "selectedAvatarId=" + selected + "\n";

            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (Exception ignored) {}
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private static Set<String> parseSet(String s) {
        Set<String> set = new HashSet<>();
        if (s == null || s.isBlank()) return set;
        Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .forEach(set::add);
        return set;
    }
}
