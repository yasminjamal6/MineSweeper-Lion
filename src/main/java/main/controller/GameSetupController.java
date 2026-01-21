package main.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.util.ResourceUtils;
import model.Avatar;
import model.Theme;
import model.ThemeColors;

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


    @FXML private ToggleButton easyBtn, mediumBtn, hardBtn;
    @FXML private TextField playerAName, playerBName;

    @FXML private HBox themePickerA, themePickerB;
    @FXML private HBox avatarPickerA, avatarPickerB;

    @FXML private VBox mainCard;
    @FXML private HBox playersRow;
    @FXML private VBox difficultyBox;

    // אם אין fx:id="startRow" ב-FXML אז זה יהיה null וזה בסדר
    @FXML private HBox startRow;

    @FXML private CheckBox playerBAICheckbox;
    @FXML private StackPane root;

    @FXML
    private void initialize() {
        SettingsController.applyThemeToRoot(root);

        if (easyBtn != null) easyBtn.setSelected(true);

        ensureAvatarDefaults();

        rebuildPicker(themePickerA, selectedThemeA, true);
        rebuildPicker(themePickerB, selectedThemeB, false);

        rebuildAvatarPicker(avatarPickerA, selectedAvatarA, true);
        rebuildAvatarPicker(avatarPickerB, selectedAvatarB, false);

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
        if (startRow != null) startRow.setOpacity(0);

        mainCard.setTranslateY(-30);
        playersRow.setTranslateY(20);
        difficultyBox.setTranslateY(30);
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
        if (!isValidName(a)) { showAlert("Names must contain only letters and numbers."); return; }
        if (!isValidLen(a)) { showAlert("Names must be 2 to 12 characters long."); return; }

        if (!playerBIsAI) {
            if (b.isEmpty()) { showAlert("Please enter a name for Player B."); return; }
            if (a.equalsIgnoreCase(b)) { showAlert("Player names must be different."); return; }
            if (!isValidName(b)) { showAlert("Names must contain only letters and numbers."); return; }
            if (!isValidLen(b)) { showAlert("Names must be 2 to 12 characters long."); return; }
        }

        Difficulty diff = Difficulty.EASY;
        if (mediumBtn != null && mediumBtn.isSelected()) diff = Difficulty.MEDIUM;
        if (hardBtn != null && hardBtn.isSelected()) diff = Difficulty.HARD;

        selectedDifficulty = diff;
        selectedPlayerAName = a;
        selectedPlayerBName = b;

        switchSceneWithFade(event, "/view/game.fxml");
    }

    private boolean isValidName(String s) {
        return s.matches("[A-Za-z0-9א-ת\u0621-\u064A ]+");
    }

    private boolean isValidLen(String s) {
        return s.length() >= 2 && s.length() <= 12;
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
}
