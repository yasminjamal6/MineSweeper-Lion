package model;

import java.util.List;

public class ShopCatalog {

    // ✅ מקור האמת לכל האווטרים (חינמיים + קנייה)
    public static final List<ShopAvatarItem> AVATARS = List.of(

            // ===== FREE AVATARS (0 coins) =====
            new ShopAvatarItem("SIMBA",  "Simba",  0, "/images/avatars/simba.png"),
            new ShopAvatarItem("NALA",   "Nala",   0, "/images/avatars/nala.png"),
            new ShopAvatarItem("MUFASA", "Mufasa", 0, "/images/avatars/mufasa.png"),
            new ShopAvatarItem("SCAR",   "Scar",   0, "/images/avatars/scar.png"),

            // ===== SHOP AVATARS =====
            new ShopAvatarItem("LION_KING",    "Lion King",    25, "/images/avatars/lion_king.png"),
            new ShopAvatarItem("GOLDEN_LION",  "Golden Lion",  30, "/images/avatars/golden_lion.png"),
            new ShopAvatarItem("SHADOW_LION",  "Shadow Lion",  60, "/images/avatars/LionKING.png"),
            new ShopAvatarItem("PUMBA",    "Pumba",    90, "/images/avatars/Pumba.png"),
            new ShopAvatarItem("LIONESS",    "lioness",    100, "/images/avatars/lioness.png")


            );

    public static ShopAvatarItem findAvatarById(String id) {
        if (id == null) return null;

        String key = id.trim();

        for (ShopAvatarItem item : AVATARS) {
            if (item != null && item.getId() != null &&
                    item.getId().equalsIgnoreCase(key)) {
                return item;
            }
        }
        return null;
    }

}
