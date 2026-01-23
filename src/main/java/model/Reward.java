package model;

public class Reward {
    public enum Type { NONE, COINS, AVATAR, EMOJI }

    public final Type type;
    public final int coins;
    public final String value;

    private Reward(Type type, int coins, String value) {
        this.type = type;
        this.coins = coins;
        this.value = value;
    }

    public static Reward none() { return new Reward(Type.NONE, 0, null); }
    public static Reward coins(int amount) { return new Reward(Type.COINS, amount, null); }
    public static Reward avatar(String id) { return new Reward(Type.AVATAR, 0, id); }
    public static Reward emoji(String emoji) { return new Reward(Type.EMOJI, 0, emoji); }
}
