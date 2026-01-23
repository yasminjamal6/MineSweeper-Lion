package model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GiftStore {
    private static final Gson GSON = new Gson();

    private static Path dir() throws IOException {
        Path dir = Paths.get(System.getProperty("user.home"), ".minesweeper-lion");
        Files.createDirectories(dir);
        return dir;
    }

    private static Path file(String playerName) throws IOException {
        String safe = (playerName == null ? "player" : playerName.trim().toLowerCase())
                .replaceAll("[^a-z0-9_-]", "_");
        return dir().resolve("gift_" + safe + ".json");
    }

    public static GiftState loadOrCreate(String playerName) {
        try (BufferedReader br = Files.newBufferedReader(file(playerName))) {
            GiftState s = GSON.fromJson(br, GiftState.class);
            if (s == null) s = new GiftState();
            return s;
        } catch (IOException | JsonSyntaxException e) {
            return new GiftState();
        }
    }

    public static void save(String playerName, GiftState state) {
        try (BufferedWriter bw = Files.newBufferedWriter(file(playerName))) {
            GSON.toJson(state, bw);
        } catch (IOException ignored) {}
    }
}
