package model;

/**
 * ProfileStore – עטיפה פשוטה לעבודה עם PlayerProfileManager
 * כל הנתונים נשמרים ב־data/player-profiles.json
 */
public class ProfileStore {

    /**
     * Loads an existing player profile or creates a new one if it does not exist.
     */
    public static PlayerProfile loadOrCreate(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            playerName = "Player";
        }

        PlayerProfile profile =
                PlayerProfileManager.getOrCreateProfile(playerName, null);

        if (profile == null) {
            profile = new PlayerProfile(playerName);
        }

        // Ensure all default fields are initialized        profile.ensureDefaults();

        return profile;
    }

    /**
     Persists changes to a player profile (coins, avatars, emojis, selected avatar, etc.).
     */
    public static void save(PlayerProfile profile) {
        if (profile == null) return;

        // Ensure profile integrity before saving
        profile.ensureDefaults();

        // Update or insert profile data and persist to JSON storage
        PlayerProfileManager.upsertProfileData(profile);
    }
}
