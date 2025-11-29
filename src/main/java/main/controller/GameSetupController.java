package main.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.control.Button;

/**
 * Controller for the Game Setup screen.
 * Handles player input, difficulty selection, and navigation to the main game scene.
 */
public class GameSetupController {
    public enum Difficulty { EASY, MEDIUM, HARD }

    public static Difficulty selectedDifficulty = Difficulty.EASY;
    public static String selectedPlayerAName;
    public static String selectedPlayerBName;
    public static Color selectedPlayerAColor = Color.web("#6C63FF");
    public static Color selectedPlayerBColor = Color.web("#FF8C42");
    private static final Color GOLD_COLOR = Color.web("#6C63FF");     // Royal Gold
    private static final Color SAVANNA_COLOR = Color.web("#FF8C42");  // Savanna Sunset


    @FXML private ToggleButton easyBtn, mediumBtn, hardBtn;
    @FXML private ToggleGroup difficultyGroup;
    @FXML private TextField playerAName, playerBName;
    @FXML private Button aGoldBtn, aSavannaBtn;
    @FXML private Button bGoldBtn, bSavannaBtn;
    @FXML private StackPane root;   // ⭐ מחובר ל-fx:id="root" ב-FXML

    /**
     * Initializes the controller. Sets the default difficulty and logs FXML/CSS loading status.
     */
    @FXML
    private void initialize() {
        // Sets 'Easy' as the default selection when the view loads.
        easyBtn.setSelected(true);
    }


    /**
     * Navigates back to the home screen with a smooth fade transition.
     */
    @FXML
    private void onBack(ActionEvent event) {
        System.out.println(">> Back clicked");

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/home-view.fxml")
            );
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();

            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(300), newRoot);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    // ---------- Player A choose color ----------
    @FXML
    private void onChooseGoldA() {
        selectedPlayerAColor = GOLD_COLOR;
        updateButtonsForConflicts();
    }

    @FXML
    private void onChooseSavannaA() {
        selectedPlayerAColor = SAVANNA_COLOR;
        updateButtonsForConflicts();
    }


    // ---------- Player B choose color ----------
    @FXML
    private void onChooseGoldB() {
        selectedPlayerBColor = GOLD_COLOR;
        updateButtonsForConflicts();
    }

    @FXML
    private void onChooseSavannaB() {
        selectedPlayerBColor = SAVANNA_COLOR;
        updateButtonsForConflicts();
    }


    // ---------- Disable same color for other player ----------
    private void updateButtonsForConflicts() {

        // להחזיר הכל למצב פעיל
        aGoldBtn.setDisable(false);
        aSavannaBtn.setDisable(false);
        bGoldBtn.setDisable(false);
        bSavannaBtn.setDisable(false);

        // אם A בחר GOLD → לנעול GOLD אצל B
        if (selectedPlayerAColor.equals(GOLD_COLOR)) {
            bGoldBtn.setDisable(true);
        }

        // אם A בחר SAVANNA → לנעול SAVANNA אצל B
        if (selectedPlayerAColor.equals(SAVANNA_COLOR)) {
            bSavannaBtn.setDisable(true);
        }

        // אם B בחר GOLD → לנעול GOLD אצל A
        if (selectedPlayerBColor.equals(GOLD_COLOR)) {
            aGoldBtn.setDisable(true);
        }

        // אם B בחר SAVANNA → לנעול SAVANNA אצל A
        if (selectedPlayerBColor.equals(SAVANNA_COLOR)) {
            aSavannaBtn.setDisable(true);
        }
    }

}
