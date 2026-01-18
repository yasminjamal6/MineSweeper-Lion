package model;

public enum Avatar {
    SIMBA("simba", "Simba", "/images/avatars/simba.png"),
    NALA("nala", "Nala", "/images/avatars/nala.png"),
    MUFASA("mufasa", "Mufasa", "/images/avatars/mufasa.jpg"),
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
