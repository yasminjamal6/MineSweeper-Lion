package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerProfile {
    private String playerName;
    private String avatarId;
    private List<MatchRecord> matches = new ArrayList<>();
    private int coins;
    private Set<String> ownedAvatars;
    private String selectedAvatarId;
    private long nextGiftEpochMillis;
    // מתי המתנה הבאה מוכנה (milliseconds)


    // ✅ חייב להתאים ל-IDs ב-ShopCatalog
    private static final Set<String> FREE_AVATARS = Set.of("SIMBA", "NALA", "MUFASA", "SCAR");
    private int winsSinceGift = 0;

    public int getWinsSinceGift() { return winsSinceGift; }
    public boolean isGiftReady() { return winsSinceGift >= 3; }

    public void onWin() { winsSinceGift = Math.min(3, winsSinceGift + 1); }
    public void consumeGift() { winsSinceGift = 0; }

    public PlayerProfile() {
        this("Player", null);
    }
    private java.util.Map<String, Integer> emojiCounts = new java.util.HashMap<>();

    public java.util.Map<String, Integer> getEmojiCounts() { return emojiCounts; }

    public void addEmoji(String emojiId, int amount) {
        emojiCounts.put(emojiId, emojiCounts.getOrDefault(emojiId, 0) + amount);
    }

    public boolean consumeEmoji(String emojiId) {
        int c = emojiCounts.getOrDefault(emojiId, 0);
        if (c <= 0) return false;
        emojiCounts.put(emojiId, c - 1);
        return true;
    }

    public PlayerProfile(String playerName) {
        this(playerName, null);
    }

    public PlayerProfile(String playerName, String avatarId) {
        this.playerName = playerName;
        this.avatarId = avatarId;
        this.coins = 0;
        this.ownedAvatars = new HashSet<>();
        ownedAvatars.addAll(FREE_AVATARS);
        if (avatarId != null && !avatarId.isBlank()) {
            ownedAvatars.add(avatarId);
            selectedAvatarId = avatarId;
        } else {
            selectedAvatarId = "SIMBA";
        }
    }

    public void ensureDefaults() {
        if (ownedAvatars == null) ownedAvatars = new HashSet<>();
        ownedAvatars.addAll(FREE_AVATARS);

        if (selectedAvatarId != null) {
            selectedAvatarId = selectedAvatarId.trim().toUpperCase();
        }

        if (selectedAvatarId == null || !ownedAvatars.contains(selectedAvatarId)) {
            selectedAvatarId = (avatarId != null && !avatarId.isBlank()) ? avatarId : "SIMBA";
        }

        if (nextGiftEpochMillis < 0) nextGiftEpochMillis = 0;
        if (emojiCounts == null) emojiCounts = new java.util.HashMap<>();
    }



    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getAvatarId() {
        if (avatarId != null && !avatarId.isBlank()) {
            return avatarId;
        }
        return selectedAvatarId;
    }
    public void setWinsSinceGift(int winsSinceGift) {
        this.winsSinceGift = Math.max(0, Math.min(3, winsSinceGift));
    }

    public void setAvatarId(String avatarId) {
        if (avatarId == null) return;
        String id = avatarId.trim().toUpperCase();

        this.avatarId = id;
        if (ownedAvatars != null) {
            ownedAvatars.add(id);
        }
        selectedAvatarId = id;
    }

    public long getNextGiftEpochMillis() {
        return nextGiftEpochMillis;
    }

    public void setNextGiftEpochMillis(long nextGiftEpochMillis) {
        this.nextGiftEpochMillis = Math.max(0, nextGiftEpochMillis);
    }


    public List<MatchRecord> getMatches() {
        if (matches == null) {
            matches = new ArrayList<>();
        }
        return matches;
    }

    public void addMatch(MatchRecord record) {
        if (record != null) {
            getMatches().add(0, record);
        }
    }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = Math.max(0, coins); }

    public void addCoins(int amount) {
        if (amount <= 0) return;
        coins += amount;
    }

    public boolean spendCoins(int amount) {
        if (amount <= 0) return true;
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }

    public Set<String> getOwnedAvatars() { return ownedAvatars; }

    public boolean ownsAvatar(String avatarId) {
        if (ownedAvatars == null || avatarId == null) return false;
        return ownedAvatars.contains(avatarId.trim().toUpperCase());
    }

    public void addAvatar(String avatarId) {
        if (avatarId == null) return;
        ownedAvatars.add(avatarId.trim().toUpperCase());
    }

    public String getSelectedAvatarId() {
        if (selectedAvatarId != null && !selectedAvatarId.isBlank()) {
            return selectedAvatarId;
        }
        return avatarId;
    }

    public boolean selectAvatar(String avatarId) {
        if (avatarId == null) return false;
        avatarId = avatarId.trim().toUpperCase();
        if (!ownsAvatar(avatarId)) return false;
        selectedAvatarId = avatarId;
        return true;
    }

}
