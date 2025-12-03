package model;

public enum RevealResult {
    IGNORED,       // Click was ignored (out of bounds / already revealed / flagged)
    HIT_MINE,      // Player revealed a mine
    EMPTY_AREA,    // Cell with 0 adjacent mines (flood fill area)
    SAFE_NUMBER    // Cell with number > 0
}
