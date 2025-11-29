package model;

public class Theme {
    public final String id;
    public final String name;
    public final String previewStyle;
    public final String cellStyle;
    public final String revealedStyle;

    public Theme(String id, String name, String previewStyle, String cellStyle, String revealedStyle) {
        this.id = id;
        this.name = name;
        this.previewStyle = previewStyle;
        this.cellStyle = cellStyle;
        this.revealedStyle = revealedStyle;
    }
}
