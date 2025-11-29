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
                    "prideRock",
                    "Pride Rock",
                    "-fx-background-color: linear-gradient(to bottom right, #78716c, #44403c);",
                    "-fx-background-color: linear-gradient(to bottom right, #78716c, #44403c);",
                    "-fx-background-color: #f5f5f4; -fx-border-color: #d6d3d1;"
            ),
            new Theme(
                    "jungle",
                    "Jungle",
                    "-fx-background-color: linear-gradient(to bottom right, #22c55e, #065f46);",
                    "-fx-background-color: linear-gradient(to bottom right, #22c55e, #065f46);",
                    "-fx-background-color: #ecfdf5; -fx-border-color: #a7f3d0;"
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
