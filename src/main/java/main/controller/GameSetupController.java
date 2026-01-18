package main.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

    @FXML private ToggleButton easyBtn, mediumBtn, hardBtn;
    @FXML private ToggleGroup difficultyGroup;
    @FXML private TextField playerAName, playerBName;

    @FXML private HBox themePickerA;
    @FXML private HBox themePickerB;
    @FXML private HBox avatarPickerA;
    @FXML private HBox avatarPickerB;
    @FXML private VBox mainCard;
    @FXML private HBox playersRow;
    @FXML private VBox difficultyBox;
    @FXML private HBox startRow;

    public static Theme selectedThemeA = ThemeColors.themes.get(0);
    public static Theme selectedThemeB = ThemeColors.themes.get(1);
    public static boolean skipResumePrompt = false;


    @FXML private StackPane root;

    /**
     * Initializes the controller. Sets the default difficulty and logs FXML/CSS loading status.
     */
    @FXML
    private void initialize() {
        SettingsController.applyThemeToRoot(root);

        easyBtn.setSelected(true);

        System.out.println("Root = " + root);
        if (root != null) {
            System.out.println("Stylesheets from FXML = " + root.getStylesheets());
        }
        System.out.println("CSS exists? -> " + getClass().getResource("/css/game-setup.css"));
        System.out.println("Image exists? -> " + getClass().getResource("/images/King.png"));

        ensureAvatarDefaults();
        rebuildPicker(themePickerA, selectedThemeA, true);
        rebuildPicker(themePickerB, selectedThemeB, false);
        rebuildAvatarPicker(avatarPickerA, selectedAvatarA, true);
        rebuildAvatarPicker(avatarPickerB, selectedAvatarB, false);

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                SettingsController.refreshLanguageOnAllWindows();
            }
        });

        playIntroAnimation();

    }



    /**
     * Navigates back to the home screen with a smooth fade transition.
     */
    @FXML
    private void onBack(ActionEvent event) {
        System.out.println(">> Back clicked");

        try {
            var url = ResourceUtils.url(getClass(), "/view/home-view.fxml");
            if (url == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent newRoot = loader.load();


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

    /**
     * Adds a smooth entrance animation similar to the home page.
     */
    private void playIntroAnimation() {
        if (mainCard == null || playersRow == null || difficultyBox == null || startRow == null) {
            return;
        }

        mainCard.setOpacity(0);
        playersRow.setOpacity(0);
        difficultyBox.setOpacity(0);
        startRow.setOpacity(0);

        mainCard.setTranslateY(-30);
        playersRow.setTranslateY(20);
        difficultyBox.setTranslateY(30);
        startRow.setTranslateY(40);

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

        FadeTransition startFade = new FadeTransition(Duration.millis(580), startRow);
        startFade.setFromValue(0);
        startFade.setToValue(1);
        startFade.setDelay(Duration.millis(340));

        TranslateTransition startSlide = new TranslateTransition(Duration.millis(580), startRow);
        startSlide.setFromY(40);
        startSlide.setToY(0);
        startSlide.setInterpolator(Interpolator.EASE_OUT);
        startSlide.setDelay(Duration.millis(340));

        new ParallelTransition(
                mainFade, mainSlide,
                playersFade, playersSlide,
                diffFade, diffSlide,
                startFade, startSlide
        ).play();
    }



    /**
     * Validates player input and starts the game if all data is valid.
     * Displays alerts for missing or duplicate player names.
     */
    @FXML
    private void onStart(ActionEvent event) {

        String a = playerAName.getText() == null ? "" : playerAName.getText().trim();
        String b = playerBName.getText() == null ? "" : playerBName.getText().trim();

        if (a.isEmpty() || b.isEmpty()) {
            showAlert("Please enter a name for both players.");
            return;
        }
        if (a.equalsIgnoreCase(b)) {
            showAlert("Player names must be different.");
            return;
        }
        // Letters/numbers only
        if (!a.matches("[A-Za-z0-9א-ת\u0621-\u064A ]+") ||
                !b.matches("[A-Za-z0-9א-ת\u0621-\u064A ]+")) {

            showAlert("Names must contain only letters and numbers.");
            return;
        }

        // Length validation
        if (a.length() < 2 || a.length() > 12 || b.length() < 2 || b.length() > 12) {
            showAlert("Names must be 2 to 12 characters long.");
            return;
        }

        Difficulty diff = Difficulty.EASY;
        if (mediumBtn.isSelected()) diff = Difficulty.MEDIUM;
        if (hardBtn.isSelected())   diff = Difficulty.HARD;

        selectedDifficulty = diff;
        selectedPlayerAName = a;
        selectedPlayerBName = b;

        switchSceneWithFade(event, "/view/game.fxml");
    }

    private void switchSceneWithFade(ActionEvent event, String fxmlPath) {
        try {
            var url = ResourceUtils.url(getClass(), fxmlPath);
            if (url == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent newRoot = loader.load();

            // Get current scene + stage
            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();

            // Fade-in transition
            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(300), newRoot);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

            // Fix window size so it can't be smaller than the content
            stage.sizeToScene();                    // fit window to newRoot size
            stage.setMinWidth(stage.getWidth());    // don't allow smaller width
            stage.setMinHeight(stage.getHeight());  // don't allow smaller height

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

        btn.setStyle(
                theme.previewStyle +
                        (isSelected
                                ? "-fx-border-color: white; -fx-border-width: 3; -fx-scale-x:1.1; -fx-scale-y:1.1;"
                                : "-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2;")
        );

        btn.setOnAction(e -> {
            boolean isInA = themePickerA.getChildren().contains(btn);

            if (isInA) {
                selectedThemeA = theme;
            } else {
                selectedThemeB = theme;
            }

            rebuildPicker(themePickerA, selectedThemeA, true);
            rebuildPicker(themePickerB, selectedThemeB, false);
        });

        return btn;
    }

    private void rebuildPicker(HBox box, Theme selected, boolean isPlayerA) {
        box.getChildren().clear();

        for (Theme t : ThemeColors.themes) {
            boolean isSelected = selected != null && t.id.equals(selected.id);
            Button btn = createThemeButton(t, isSelected);

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
        if (box == null) {
            return;
        }
        box.getChildren().clear();

        ToggleGroup group = new ToggleGroup();
        for (Avatar avatar : Avatar.values()) {
            boolean isSelected = selected != null && avatar == selected;
            ToggleButton btn = createAvatarButton(avatar, isSelected, isPlayerA, group);

            boolean usedByOther = isPlayerA
                    ? (selectedAvatarB != null && avatar == selectedAvatarB)
                    : (selectedAvatarA != null && avatar == selectedAvatarA);

            if (usedByOther && !isSelected) {
                btn.setDisable(true);
                btn.setOpacity(0.45);
            }

            box.getChildren().add(btn);
        }
    }

    private ToggleButton createAvatarButton(Avatar avatar, boolean isSelected, boolean isPlayerA, ToggleGroup group) {
        ToggleButton btn = new ToggleButton();
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
        if (avatar == null) {
            return null;
        }
        try (var is = ResourceUtils.stream(getClass(), avatar.resourcePath)) {
            if (is == null) {
                return null;
            }
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
        if (selectedAvatarA == null) {
            selectedAvatarA = Avatar.SIMBA;
        }
        if (selectedAvatarB == null || selectedAvatarB == selectedAvatarA) {
            selectedAvatarB = selectedAvatarA == Avatar.NALA ? Avatar.MUFASA : Avatar.NALA;
        }
    }

}
