package model;

public class ShopAvatarItem {
    private final String id;        // למשל: "LION_KING"
    private final String name;      // שם לתצוגה
    private final int price;        // מחיר בקוינס
    private final String imagePath; // למשל: "/images/avatars/lion_king.png"

    public ShopAvatarItem(String id, String name, int price, String imagePath) {
        this.id = id;
        this.name = name;
        this.price = Math.max(0, price);
        this.imagePath = imagePath;
    }

    public String getId() { return id; }

    // השם המקורי שלך
    public String getName() { return name; }

    // ✅ כדי שהקונטרולרים שלך יעבדו (כמו שכתבת)
    public String getDisplayName() { return name; }

    public int getPrice() { return price; }

    public String getImagePath() { return imagePath; }
}
