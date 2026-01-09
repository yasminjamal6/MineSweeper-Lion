package main.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextLimitUtilTest {

    @Test
    public void truncateInsertedText_allowsWithinLimit() {
        String result = TextLimitUtil.truncateInsertedText("abc", 3, 3, "def", 10);
        assertEquals("def", result);
    }

    @Test
    public void truncateInsertedText_truncatesOverLimit() {
        String result = TextLimitUtil.truncateInsertedText("12345", 5, 5, "abcdef", 7);
        assertEquals("ab", result);
    }

    @Test
    public void truncateInsertedText_truncatesForReplacement() {
        String result = TextLimitUtil.truncateInsertedText("hello", 1, 3, "WORLD", 6);
        assertEquals("WOR", result);
    }

    @Test
    public void truncateInsertedText_returnsEmptyWhenNoSpace() {
        String result = TextLimitUtil.truncateInsertedText("12345", 5, 5, "x", 5);
        assertEquals("", result);
    }
}
