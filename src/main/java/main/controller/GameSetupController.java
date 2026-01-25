package main.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.util.ResourceUtils;
import model.Avatar;
import model.PlayerProfileManager;
import model.Theme;
import model.ThemeColors;
import model.Session;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GameSetupController {

    public enum Difficulty { EASY, MEDIUM, HARD }

    public static Difficulty selectedDifficulty = Difficulty.EASY;
    public static String selectedPlayerAName;
    public static String selectedPlayerBName;

    public static Avatar selectedAvatarA = Avatar.SIMBA;
    public static Avatar selectedAvatarB = Avatar.NALA;

    public static Theme selectedThemeA = ThemeColors.themes.get(0);
    public static Theme selectedThemeB = ThemeColors.themes.get(1);

    public static boolean playerBIsAI = false;
    public static boolean skipResumePrompt = false;

    private static final String USERNAME_REGEX = "^[a-zA-Z][a-zA-Z0-9._-]{2,19}$";

    private static final List<LevelRuleSpec> LEVEL_RULES = List.of(
            new LevelRuleSpec(Difficulty.EASY, "Easy", "🟢",
                    9, 9, 10, 6, 2, 10, 5, 8, -8),
            new LevelRuleSpec(Difficulty.MEDIUM, "Medium", "🟠",
                    13, 13, 26, 7, 3, 8, 8, 12, -12),
            new LevelRuleSpec(Difficulty.HARD, "Hard", "🔴",
                    16, 16, 44, 11, 4, 6, 12, 16, -16)
    );

    @FXML private ToggleButton easyBtn, mediumBtn, hardBtn;
    @FXML private TextField playerAName, playerBName;
    @FXML private ToggleGroup difficultyGroup;

    @FXML private HBox themePickerA, themePickerB;
    @FXML private HBox avatarPickerA, avatarPickerB;

    @FXML private VBox mainCard;
    @FXML private HBox playersRow;
    @FXML private VBox difficultyBox;
    @FXML private VBox levelRulesBox;
    @FXML private Accordion levelRulesAccordion;

    // אם אין fx:id="startRow" ב-FXML אז זה יהיה null וזה בסדר
    @FXML private HBox startRow;

    @FXML private CheckBox playerBAICheckbox;
    @FXML private StackPane root;

    private final Map<Difficulty, TitledPane> rulePanes = new EnumMap<>(Difficulty.class);

    @FXML
    private void initialize() {
        SettingsController.applyThemeToRoot(root);

        if (easyBtn != null) easyBtn.setSelected(true);

        ensureAvatarDefaults();

        rebuildPicker(themePickerA, selectedThemeA, true);
        rebuildPicker(themePickerB, selectedThemeB, false);

        rebuildAvatarPicker(avatarPickerA, selectedAvatarA, true);
        rebuildAvatarPicker(avatarPickerB, selectedAvatarB, false);

        setupDifficultySync();
        buildLevelRulesAccordion();

        // אם המסך נפתח כשהצ׳קבוקס כבר מסומן
        onTogglePlayerBAI();

        if (root != null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) SettingsController.refreshLanguageOnAllWindows();
            });
        }

        playIntroAnimation();
    }

    @FXML
    private void onBack(ActionEvent event) {
        try {
            var url = ResourceUtils.url(getClass(), "/view/home-view.fxml");
            if (url == null) return;

            Parent newRoot = new FXMLLoader(url).load();

            Scene scene = ((Node) event.getSource()).getScene();
            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(250), newRoot);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playIntroAnimation() {
        if (mainCard == null || playersRow == null || difficultyBox == null) return;

        mainCard.setOpacity(0);
        playersRow.setOpacity(0);
        difficultyBox.setOpacity(0);
        if (levelRulesBox != null) levelRulesBox.setOpacity(0);
        if (startRow != null) startRow.setOpacity(0);

        mainCard.setTranslateY(-30);
        playersRow.setTranslateY(20);
        difficultyBox.setTranslateY(30);
        if (levelRulesBox != null) levelRulesBox.setTranslateY(32);
        if (startRow != null) startRow.setTranslateY(40);

        FadeTransition mainFade = new FadeTransition(Duration.millis(520), mainCard);
        mainFade.setFromValue(0);
        mainFade.setToValue(1);

        TranslateTransition mainSlide = new TranslateTransition(Duration.millis(520), mainCard);
        mainSlide.setFromY(-30);
        mainSlide.setToY(0);
        mainSlide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition playersFade = new FadeTransition(Duration.millis(540), playersRow);
        playersFade.setFromValue(0);
        playersFade.setToValue(1);
        playersFade.setDelay(Duration.millis(120));

        TranslateTransition playersSlide = new TranslateTransition(Duration.millis(540), playersRow);
        playersSlide.setFromY(20);
        playersSlide.setToY(0);
        playersSlide.setInterpolator(Interpolator.EASE_OUT);
        playersSlide.setDelay(Duration.millis(120));

        FadeTransition diffFade = new FadeTransition(Duration.millis(560), difficultyBox);
        diffFade.setFromValue(0);
        diffFade.setToValue(1);
        diffFade.setDelay(Duration.millis(240));

        TranslateTransition diffSlide = new TranslateTransition(Duration.millis(560), difficultyBox);
        diffSlide.setFromY(30);
        diffSlide.setToY(0);
        diffSlide.setInterpolator(Interpolator.EASE_OUT);
        diffSlide.setDelay(Duration.millis(240));

        ParallelTransition pt = new ParallelTransition(mainFade, mainSlide, playersFade, playersSlide, diffFade, diffSlide);

        if (levelRulesBox != null) {
            FadeTransition rulesFade = new FadeTransition(Duration.millis(580), levelRulesBox);
            rulesFade.setFromValue(0);
            rulesFade.setToValue(1);
            rulesFade.setDelay(Duration.millis(300));

            TranslateTransition rulesSlide = new TranslateTransition(Duration.millis(580), levelRulesBox);
            rulesSlide.setFromY(32);
            rulesSlide.setToY(0);
            rulesSlide.setInterpolator(Interpolator.EASE_OUT);
            rulesSlide.setDelay(Duration.millis(300));

            pt.getChildren().addAll(rulesFade, rulesSlide);
        }

        if (startRow != null) {
            FadeTransition startFade = new FadeTransition(Duration.millis(580), startRow);
            startFade.setFromValue(0);
            startFade.setToValue(1);
            startFade.setDelay(Duration.millis(340));

            TranslateTransition startSlide = new TranslateTransition(Duration.millis(580), startRow);
            startSlide.setFromY(40);
            startSlide.setToY(0);
            startSlide.setInterpolator(Interpolator.EASE_OUT);
            startSlide.setDelay(Duration.millis(340));

            pt.getChildren().addAll(startFade, startSlide);
        }

        pt.play();
    }

    @FXML
    private void onStart(ActionEvent event) {
        String a = playerAName.getText() == null ? "" : playerAName.getText().trim();
        playerBIsAI = playerBAICheckbox != null && playerBAICheckbox.isSelected();

        String b = playerBIsAI ? "Lion AI"
                : (playerBName.getText() == null ? "" : playerBName.getText().trim());

        if (a.isEmpty()) { showAlert("Please enter a name for Player A."); return; }
        if (isNameTakenWithDifferentCase(a)) {
            showAlert("Username already taken. Choose a different name.");
            return;
        }
        if (!isExistingProfileName(a) && !isValidUsernameFormat(a)) {
            showAlert("Invalid username. Use 3-20 characters, start with a letter, and only letters, numbers, dot, underscore, or dash.");
            return;
        }

        if (!playerBIsAI) {
            if (b.isEmpty()) { showAlert("Please enter a name for Player B."); return; }
            if (a.equalsIgnoreCase(b)) { showAlert("Player names must be different."); return; }
            if (isNameTakenWithDifferentCase(b)) {
                showAlert("Username already taken. Choose a different name.");
                return;
            }
            if (!isExistingProfileName(b) && !isValidUsernameFormat(b)) {
                showAlert("Invalid username. Use 3-20 characters, start with a letter, and only letters, numbers, dot, underscore, or dash.");
                return;
            }
        }
        Session.setActivePlayerName(a);

        Difficulty diff = Difficulty.EASY;
        if (mediumBtn != null && mediumBtn.isSelected()) diff = Difficulty.MEDIUM;
        if (hardBtn != null && hardBtn.isSelected()) diff = Difficulty.HARD;

        selectedDifficulty = diff;
        selectedPlayerAName = a;
        selectedPlayerBName = b;

        switchSceneWithFade(event, "/view/game.fxml");
    }

    private boolean isValidUsernameFormat(String s) {
        return s != null && s.matches(USERNAME_REGEX);
    }

    private boolean isExistingProfileName(String s) {
        return findExistingProfileName(s) != null;
    }

    private boolean isNameTakenWithDifferentCase(String s) {
        String existing = findExistingProfileName(s);
        return existing != null && !existing.equals(s);
    }

    private String findExistingProfileName(String s) {
        if (s == null || s.isBlank()) return null;
        String target = s.trim();
        for (var profile : PlayerProfileManager.getProfiles()) {
            if (profile == null || profile.getPlayerName() == null) continue;
            if (profile.getPlayerName().equalsIgnoreCase(target)) {
                return profile.getPlayerName();
            }
        }
        return null;
    }

    @FXML
    private void onTogglePlayerBAI() {
        playerBIsAI = playerBAICheckbox != null && playerBAICheckbox.isSelected();

        if (playerBIsAI) {
            if (playerBName != null) {
                playerBName.setText("Lion AI");
                playerBName.setDisable(true);
            }
            if (avatarPickerB != null) avatarPickerB.setDisable(true);
            if (themePickerB != null) themePickerB.setDisable(true);
        } else {
            if (playerBName != null) {
                playerBName.setDisable(false);
                String cur = playerBName.getText() == null ? "" : playerBName.getText().trim();
                if ("Lion AI".equalsIgnoreCase(cur)) playerBName.clear();
            }
            if (avatarPickerB != null) avatarPickerB.setDisable(false);
            if (themePickerB != null) themePickerB.setDisable(false);
        }
    }

    private void switchSceneWithFade(ActionEvent event, String fxmlPath) {
        try {
            var url = ResourceUtils.url(getClass(), fxmlPath);
            if (url == null) return;

            Parent newRoot = new FXMLLoader(url).load();

            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();

            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(300), newRoot);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            stage.sizeToScene();
            stage.setMinWidth(stage.getWidth());
            stage.setMinHeight(stage.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        var dialogPane = alert.getDialogPane();
        String alertCss = ResourceUtils.externalForm(getClass(), "/css/alert.css");
        if (alertCss != null) {
            dialogPane.getStylesheets().add(alertCss);
        }
        dialogPane.getStyleClass().add("lion-alert");

        alert.showAndWait();
    }


    private Button createThemeButton(Theme theme, boolean isSelected) {
        Button btn = new Button();
        btn.setPrefSize(40, 40);
        btn.getStyleClass().add("theme-btn");
        btn.setStyle(theme.previewStyle + (isSelected
                ? "-fx-border-color: white; -fx-border-width: 3; -fx-scale-x:1.1; -fx-scale-y:1.1;"
                : "-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2;"));

        btn.setOnAction(e -> {
            boolean isInA = themePickerA != null && themePickerA.getChildren().contains(btn);
            if (isInA) selectedThemeA = theme;
            else selectedThemeB = theme;

            rebuildPicker(themePickerA, selectedThemeA, true);
            rebuildPicker(themePickerB, selectedThemeB, false);
        });

        return btn;
    }

    private void rebuildPicker(HBox box, Theme selected, boolean isPlayerA) {
        if (box == null) return;

        box.getChildren().clear();
        for (Theme t : ThemeColors.themes) {
            boolean sel = selected != null && t.id.equals(selected.id);
            Button btn = createThemeButton(t, sel);

            boolean usedByOther = isPlayerA
                    ? (selectedThemeB != null && t.id.equals(selectedThemeB.id))
                    : (selectedThemeA != null && t.id.equals(selectedThemeA.id));

            if (usedByOther) {
                btn.setDisable(true);
                btn.setOpacity(0.35);
            }

            box.getChildren().add(btn);
        }
    }

    private void rebuildAvatarPicker(HBox box, Avatar selected, boolean isPlayerA) {
        if (box == null) return;

        box.getChildren().clear();
        ToggleGroup group = new ToggleGroup();

        for (Avatar avatar : Avatar.values()) {
            boolean sel = selected != null && avatar == selected;
            ToggleButton btn = createAvatarButton(avatar, sel, isPlayerA, group);

            boolean usedByOther = isPlayerA
                    ? (selectedAvatarB != null && avatar == selectedAvatarB)
                    : (selectedAvatarA != null && avatar == selectedAvatarA);

            if (usedByOther && !sel) {
                btn.setDisable(true);
                btn.setOpacity(0.45);
            }

            box.getChildren().add(btn);
        }
    }

    private ToggleButton createAvatarButton(Avatar avatar, boolean isSelected, boolean isPlayerA, ToggleGroup group) {
        ToggleButton btn = new ToggleButton();

        // ✅ Keep fixed size (prevents shrinking)
        btn.setPrefSize(56, 56);
        btn.setMinSize(56, 56);
        btn.setMaxSize(56, 56);

        btn.getStyleClass().add("avatar-btn");
        btn.setToggleGroup(group);
        btn.setSelected(isSelected);

        ImageView view = loadAvatarView(avatar);
        if (view != null) {
            btn.setGraphic(view);
        } else {
            btn.setText(avatar.displayName);
        }

        // ✅ Tooltip back (nice UX)
        btn.setTooltip(new Tooltip(avatar.displayName));

        btn.setOnAction(e -> {
            if (isPlayerA) {
                selectedAvatarA = avatar;
            } else {
                selectedAvatarB = avatar;
            }

            rebuildAvatarPicker(avatarPickerA, selectedAvatarA, true);
            rebuildAvatarPicker(avatarPickerB, selectedAvatarB, false);
        });

        return btn;
    }


    private ImageView loadAvatarView(Avatar avatar) {
        if (avatar == null) return null;

        try (var is = ResourceUtils.stream(getClass(), avatar.resourcePath)) {
            if (is == null) return null;
            Image image = new Image(is);
            ImageView view = new ImageView(image);
            view.setFitWidth(42);
            view.setFitHeight(42);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            return view;
        } catch (Exception e) {
            return null;
        }
    }

    private void ensureAvatarDefaults() {
        if (selectedAvatarA == null) selectedAvatarA = Avatar.SIMBA;
        if (selectedAvatarB == null || selectedAvatarB == selectedAvatarA) {
            selectedAvatarB = (selectedAvatarA == Avatar.NALA) ? Avatar.MUFASA : Avatar.NALA;
        }
    }

    private void setupDifficultySync() {
        if (difficultyGroup == null) return;
        difficultyGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            Difficulty diff = Difficulty.EASY;
            if (newToggle == mediumBtn) diff = Difficulty.MEDIUM;
            if (newToggle == hardBtn) diff = Difficulty.HARD;
            selectedDifficulty = diff;
            expandRuleFor(diff);
        });
    }

    private void buildLevelRulesAccordion() {
        if (levelRulesAccordion == null) return;

        levelRulesAccordion.getPanes().clear();
        rulePanes.clear();

        for (LevelRuleSpec spec : LEVEL_RULES) {
            TitledPane pane = createRulePane(spec);
            rulePanes.put(spec.difficulty, pane);
            levelRulesAccordion.getPanes().add(pane);
        }

        levelRulesAccordion.expandedPaneProperty().addListener((obs, oldPane, newPane) -> {
            updateRuleHighlight(newPane);
            if (newPane != null) {
                Difficulty match = findDifficultyForPane(newPane);
                if (match != null) selectDifficultyButton(match);
            }
        });

        expandRuleFor(selectedDifficulty != null ? selectedDifficulty : Difficulty.EASY);
    }

    private void expandRuleFor(Difficulty difficulty) {
        if (levelRulesAccordion == null || difficulty == null) return;
        TitledPane pane = rulePanes.get(difficulty);
        if (pane != null) {
            levelRulesAccordion.setExpandedPane(pane);
        }
        updateRuleHighlight(levelRulesAccordion.getExpandedPane());
    }

    private void updateRuleHighlight(TitledPane expandedPane) {
        for (TitledPane pane : rulePanes.values()) {
            pane.getStyleClass().remove("level-rule-card-selected");
        }
        if (expandedPane != null) {
            expandedPane.getStyleClass().add("level-rule-card-selected");
        }
    }

    private Difficulty findDifficultyForPane(TitledPane pane) {
        for (Map.Entry<Difficulty, TitledPane> entry : rulePanes.entrySet()) {
            if (entry.getValue() == pane) return entry.getKey();
        }
        return null;
    }

    private void selectDifficultyButton(Difficulty difficulty) {
        if (difficulty == null) return;
        if (difficulty == Difficulty.EASY && easyBtn != null) easyBtn.setSelected(true);
        if (difficulty == Difficulty.MEDIUM && mediumBtn != null) mediumBtn.setSelected(true);
        if (difficulty == Difficulty.HARD && hardBtn != null) hardBtn.setSelected(true);
        selectedDifficulty = difficulty;
    }

    private TitledPane createRulePane(LevelRuleSpec spec) {
        TitledPane pane = new TitledPane();
        pane.setAnimated(true);
        pane.setCollapsible(true);
        pane.setFocusTraversable(true);
        pane.getStyleClass().add("level-rule-card");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("level-rule-header");

        Label icon = new Label(spec.icon);
        icon.getStyleClass().add("level-rule-header-icon");
        Label title = new Label(spec.title);
        title.getStyleClass().add("level-rule-header-title");

        header.getChildren().addAll(icon, title);
        pane.setText("");
        pane.setGraphic(header);

        VBox content = new VBox(6);
        content.setPadding(new Insets(10, 12, 12, 12));
        content.getStyleClass().add("level-rule-content");

        content.getChildren().addAll(
                createRuleRow("🧩", "Board", spec.rows + "x" + spec.cols),
                createRuleRow("💣", "Mines", String.valueOf(spec.mines)),
                createRuleRow("❓", "Question", String.valueOf(spec.questions)),
                createRuleRow("🎁", "Surprise", String.valueOf(spec.surprises)),
                createRuleRow("❤️", "Hearts", String.valueOf(spec.hearts)),
                createRuleRow("💰", "Cost", spec.activationCost + " points"),
                createRuleRow("🎲", "Surprise effect",
                        "Good: +1 heart, +" + spec.surpriseGood + " points\nBad: -1 heart, " + spec.surpriseBad + " points")
        );

        pane.setContent(content);
        pane.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
            if (isExpanded) {
                content.setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(220), content);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            } else {
                content.setOpacity(0);
            }
        });

        pane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                if (pane.isExpanded()) {
                    pane.setExpanded(false);
                } else if (levelRulesAccordion != null) {
                    levelRulesAccordion.setExpandedPane(pane);
                } else {
                    pane.setExpanded(true);
                }
                event.consume();
            }
        });

        return pane;
    }

    private HBox createRuleRow(String iconText, String labelText, String valueText) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("level-rule-row");

        Label icon = new Label(iconText);
        icon.getStyleClass().add("level-rule-row-icon");

        Label text = new Label(labelText + ": " + valueText);
        text.setWrapText(true);
        text.getStyleClass().add("level-rule-row-text");

        row.getChildren().addAll(icon, text);
        return row;
    }

    private static final class LevelRuleSpec {
        private final Difficulty difficulty;
        private final String title;
        private final String icon;
        private final int rows;
        private final int cols;
        private final int mines;
        private final int questions;
        private final int surprises;
        private final int hearts;
        private final int activationCost;
        private final int surpriseGood;
        private final int surpriseBad;

        private LevelRuleSpec(Difficulty difficulty,
                              String title,
                              String icon,
                              int rows,
                              int cols,
                              int mines,
                              int questions,
                              int surprises,
                              int hearts,
                              int activationCost,
                              int surpriseGood,
                              int surpriseBad) {
            this.difficulty = difficulty;
            this.title = title;
            this.icon = icon;
            this.rows = rows;
            this.cols = cols;
            this.mines = mines;
            this.questions = questions;
            this.surprises = surprises;
            this.hearts = hearts;
            this.activationCost = activationCost;
            this.surpriseGood = surpriseGood;
            this.surpriseBad = surpriseBad;
        }
    }
}
