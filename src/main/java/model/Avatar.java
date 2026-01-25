package model;


/**
 * Represents a player avatar with an internal id, display name, and image resource path.
 * Allows restoring an avatar by a saved id, with a fallback when no match is found.
 */

public enum Avatar {
    SIMBA("simba", "Simba", "/images/avatars/simba.png"),
    NALA("nala", "Nala", "/images/avatars/nala.png"),
    MUFASA("mufasa", "Mufasa", "/images/avatars/mufasa.png"),
    SCAR("scar", "Scar", "/images/avatars/scar.png");

    public final String id;
    public final String displayName;
    public final String resourcePath;

    Avatar(String id, String displayName, String resourcePath) {
        this.id = id;
        this.displayName = displayName;
        this.resourcePath = resourcePath;
    }

    public static Avatar fromId(String id, Avatar fallback) {
        if (id == null || id.isBlank()) {
            return fallback;
        }
        for (Avatar avatar : values()) {
            if (avatar.id.equalsIgnoreCase(id)) {
                return avatar;
            }
        }
        return fallback;
    }
}
