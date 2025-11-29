package model;

import java.util.List;

public class ThemeColors {
    public static final List<Theme> themes = List.of(
            new Theme(
                    "savanna",
                    "Savanna",
                    "-fx-background-color: linear-gradient(to bottom right, #f59e0b, #ea580c);",
                    "-fx-background-color: linear-gradient(to bottom right, #f59e0b, #ea580c);",
                    "-fx-background-color: #fff7ed; -fx-border-color: #fed7aa;"
            ),
            new Theme(
                    "lionKing",
                    "Lion King",
                    "-fx-background-color: linear-gradient(to bottom right, #fbbf24, #b45309, #78350f);",
                    "-fx-background-color: linear-gradient(to bottom right, #fbbf24, #b45309, #78350f);",
                    "-fx-background-color: #fef9c3; -fx-border-color: #fcd34d;"
            ),
            new Theme(
                    "royalTwilight",
                    "Royal Twilight",
                    "-fx-background-color: linear-gradient(to bottom right, #a855f7, #ec4899);",
                    "-fx-background-color: linear-gradient(to bottom right, #a855f7, #ec4899);",
                    "-fx-background-color: #fdf2f8; -fx-border-color: #fbcfe8;"
            ),

            new Theme(
                    "sunset",
                    "Sunset",
                    "-fx-background-color: linear-gradient(to bottom right, #fb923c, #dc2626);",
                    "-fx-background-color: linear-gradient(to bottom right, #fb923c, #dc2626);",
                    "-fx-background-color: #fff7ed; -fx-border-color: #fed7aa;"
            ),
            new Theme(
                    "night",
                    "Night Sky",
                    "-fx-background-color: linear-gradient(to bottom right, #6366f1, #7c3aed);",
                    "-fx-background-color: linear-gradient(to bottom right, #6366f1, #7c3aed);",
                    "-fx-background-color: #eef2ff; -fx-border-color: #c7d2fe;"
            ),
            new Theme(
                    "desert",
                    "Desert",
                    "-fx-background-color: linear-gradient(to bottom right, #facc15, #d97706);",
                    "-fx-background-color: linear-gradient(to bottom right, #facc15, #d97706);",
                    "-fx-background-color: #fefce8; -fx-border-color: #fde68a;"
            )
    );
}
