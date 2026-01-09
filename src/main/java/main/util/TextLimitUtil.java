package main.util;

public final class TextLimitUtil {

    private TextLimitUtil() {
    }

    public static String truncateInsertedText(String existingText,
                                              int rangeStart,
                                              int rangeEnd,
                                              String insertedText,
                                              int maxChars) {
        String safeExisting = existingText == null ? "" : existingText;
        String safeInserted = insertedText == null ? "" : insertedText;
        int safeStart = Math.max(0, rangeStart);
        int safeEnd = Math.min(Math.max(rangeEnd, safeStart), safeExisting.length());
        int replacedLength = safeEnd - safeStart;
        int baseLength = safeExisting.length() - replacedLength;
        int allowed = maxChars - baseLength;
        if (allowed <= 0) {
            return "";
        }
        if (safeInserted.length() <= allowed) {
            return safeInserted;
        }
        return safeInserted.substring(0, allowed);
    }
}
