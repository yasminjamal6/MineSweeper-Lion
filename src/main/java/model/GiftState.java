package model;

public class GiftState {
    public long nextAvailableEpochMillis;  // מתי אפשר לפתוח שוב
    public long lastClaimEpochMillis;      // מתי פתחו בפעם האחרונה (אופציונלי)

    public GiftState() {}
}
