package model;

import java.util.HashSet;
import java.util.Set;

public class PlayerProfile {
    private String playerName;
    private int coins;
    private Set<String> ownedAvatars;
    private String selectedAvatarId;

    // ✅ חייב להתאים ל-IDs ב-ShopCatalog
    private static final Set<String> FREE_AVATARS = Set.of("SIMBA", "NALA", "MUFASA", "SCAR");

    public PlayerProfile() {
        this("Player");
    }

    public PlayerProfile(String playerName) {
        this.playerName = playerName;
        this.coins = 0;
        this.ownedAvatars = new HashSet<>();

        ownedAvatars.addAll(FREE_AVATARS);
        selectedAvatarId = "SIMBA";
    }

    // ✅ לקרוא אחרי טעינה מהקובץ
    public void ensureDefaults() {
        if (ownedAvatars == null) ownedAvatars = new HashSet<>();
        ownedAvatars.addAll(FREE_AVATARS);

        if (selectedAvatarId == null || !ownedAvatars.contains(selectedAvatarId)) {
            selectedAvatarId = "SIMBA";
        }
    }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

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
        return ownedAvatars != null && ownedAvatars.contains(avatarId);
    }

    public void addAvatar(String avatarId) {
        if (avatarId == null) return;
        ownedAvatars.add(avatarId);
    }

    public String getSelectedAvatarId() { return selectedAvatarId; }

    public boolean selectAvatar(String avatarId) {
        if (avatarId == null) return false;
        if (!ownsAvatar(avatarId)) return false;
        selectedAvatarId = avatarId;
        return true;
    }
}
