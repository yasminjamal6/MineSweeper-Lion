package main.controller;

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
import javafx.stage.Stage;

public class GameSetupController {

    public enum Difficulty { EASY, MEDIUM, HARD }

    public static Difficulty selectedDifficulty = Difficulty.EASY;
    public static String selectedPlayerAName;
    public static String selectedPlayerBName;
    public static Color selectedPlayerAColor = Color.web("#6C63FF");
    public static Color selectedPlayerBColor = Color.web("#FF8C42");

    @FXML private ToggleButton easyBtn, mediumBtn, hardBtn;
    @FXML private ToggleGroup difficultyGroup;
    @FXML private TextField playerAName, playerBName;

    @FXML private StackPane root;   // ⭐ מחובר ל-fx:id="root" ב-FXML

    @FXML
    private void initialize() {
        // מה שהיה לך קודם
        easyBtn.setSelected(true);

        // ⭐ בדיקות כדי לראות אם ה-CSS והתמונה נטענים
        System.out.println("Root = " + root);
        if (root != null) {
            System.out.println("Stylesheets from FXML = " + root.getStylesheets());
        }
        System.out.println("CSS exists? -> " + getClass().getResource("/css/game-setup.css"));
        System.out.println("Image exists? -> " + getClass().getResource("/images/King.png"));
    }

    @FXML
    private void onBack(ActionEvent event) {
        System.out.println(">> Back clicked");

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Home-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 750));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void onStart() {
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

        switchScene("main-view.fxml");
    }

    private void switchScene(String fxmlName) {
        try {
            Stage stage = (Stage) playerAName.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/" + fxmlName));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
