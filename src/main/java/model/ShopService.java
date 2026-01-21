package model;


import java.util.List;

/**
 * Handles shop logic: listing items and purchasing avatars.
 * No UI code here – pure business logic.
 */
public class ShopService {

    /**
     * @return all avatar items available in the shop
     */
    public List<ShopAvatarItem> getAllAvatars() {
        return ShopCatalog.AVATARS;
    }

    /**
     * Attempts to purchase an avatar for the given player profile.
     *
     * @param profile  the player profile
     * @param avatarId the avatar ID to purchase
     * @return result of the purchase
     */
    public PurchaseResult purchaseAvatar(PlayerProfile profile, String avatarId) {

        // Safety checks
        if (profile == null || avatarId == null) {
            return PurchaseResult.ITEM_NOT_FOUND;
        }

        // Find item in catalog
        ShopAvatarItem item = ShopCatalog.findAvatarById(avatarId);
        if (item == null) {
            return PurchaseResult.ITEM_NOT_FOUND;
        }

        // Already owned?
        if (profile.ownsAvatar(avatarId)) {
            return PurchaseResult.ALREADY_OWNED;
        }

        int price = item.getPrice();

        // Try to pay
        boolean paid = profile.spendCoins(price);
        if (!paid) {
            return PurchaseResult.NOT_ENOUGH_COINS;
        }


        // Grant avatar
        profile.addAvatar(avatarId);

        // Persist profile
        ProfileStore.save(profile);

        return PurchaseResult.SUCCESS;
    }
}
