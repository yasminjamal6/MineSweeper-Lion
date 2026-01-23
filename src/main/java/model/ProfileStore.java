package model;

/**
 * ProfileStore – עטיפה פשוטה לעבודה עם PlayerProfileManager
 * כל הנתונים נשמרים ב־data/player-profiles.json
 */
public class ProfileStore {

    /**
     * טוען פרופיל קיים או יוצר חדש אם לא קיים
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

        // ודא שכל שדות ברירת המחדל קיימים
        profile.ensureDefaults();

        return profile;
    }

    /**
     * שמירת שינויים בפרופיל (Coins, Avatars, SelectedAvatar, Emojis וכו')
     * לא נוגע בהיסטוריית משחקים
     */
    public static void save(PlayerProfile profile) {
        if (profile == null) return;

        profile.ensureDefaults();

        // מעדכן את הנתונים בפרופיל הקיים ושומר ל־JSON
        PlayerProfileManager.upsertProfileData(profile);
    }
}
