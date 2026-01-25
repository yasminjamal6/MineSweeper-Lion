package model;

import java.util.concurrent.ThreadLocalRandom;

public class GiftManager {

    public static final long COOLDOWN_MILLIS = 3L * 24 * 60 * 60 * 1000; // 3 ימים
    public static final int OPEN_NOW_COST = 10;

    /** Returns true if the gift can be claimed now (cooldown passed). */
    public static boolean isReady(GiftState s) {
        long now = System.currentTimeMillis();
        return s.nextAvailableEpochMillis <= now;
    }

    /** Returns remaining cooldown time in milliseconds (0 if ready). */
    public static long remainingMillis(GiftState s) {
        long now = System.currentTimeMillis();
        return Math.max(0, s.nextAvailableEpochMillis - now);
    }

    /**
     * Opens a gift for the given player.
     * If payNow==true and gift not ready, tries to charge OPEN_NOW_COST coins.
     * Updates GiftState timings, applies the reward to the profile, saves profile+gift state,
     * and returns a Reward describing what was granted.
     */
    public static Reward openGift(String playerName, PlayerProfile profile, GiftState s, boolean payNow) {
        if (profile == null || s == null) return Reward.none();

        if (!isReady(s)) {
            if (!payNow) return Reward.none();
            if (!profile.spendCoins(OPEN_NOW_COST)) return Reward.none(); // אין מספיק
        }

        // ✅ קובעים את הפתיחה הבאה
        long now = System.currentTimeMillis();
        s.lastClaimEpochMillis = now;
        s.nextAvailableEpochMillis = now + COOLDOWN_MILLIS;

        // 🎁 בחירת מתנה רנדומלית
        int roll = ThreadLocalRandom.current().nextInt(100);

        Reward reward;
        if (roll < 50) {
            // 50% Coins
            int coins = ThreadLocalRandom.current().nextInt(5, 31); // 5-30
            profile.addCoins(coins);
            reward = Reward.coins(coins);

        } else if (roll < 85) {
            // 35% Emoji cosmetic
            String emoji = pickRandomEmoji();
            reward = Reward.emoji(emoji);

        } else {
            // 15% Avatar (רק אם אין אותו כבר)
            String avatarId = pickRandomAvatarId();
            if (!profile.ownsAvatar(avatarId)) {
                profile.addAvatar(avatarId);
                reward = Reward.avatar(avatarId);
            } else {
                // אם כבר יש—מפצים בקוינס
                profile.addCoins(15);
                reward = Reward.coins(15);
            }
        }

        // שומרים
        ProfileStore.save(profile);
        GiftStore.save(playerName, s);

        return reward;
    }

    private static String pickRandomEmoji() {
        String[] arr = {"🦁","🔥","👑","✨","🎯","💎","🌟"};
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    private static String pickRandomAvatarId() {
        // חייב להיות קיים ב-ShopCatalog
        String[] arr = {"LION_KING","GOLDEN_LION","SHADOW_LION"};
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }
}
