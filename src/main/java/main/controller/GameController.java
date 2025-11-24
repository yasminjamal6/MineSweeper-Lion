package main.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class GameController {

    @FXML private Label livesLabel;
    @FXML private Label scoreLabel;

    @FXML private Label playerANameLabel;
    @FXML private Label playerBNameLabel;
    @FXML private Label playerAMinesLabel;
    @FXML private Label playerBMinesLabel;

    @FXML private GridPane boardAGrid;
    @FXML private GridPane boardBGrid;

    private int lives = 10;
    private int score = 0;

    @FXML
    private void initialize() {
        // Set player names from setup screen
        playerANameLabel.setText(GameSetupController.selectedPlayerAName);
        playerBNameLabel.setText(GameSetupController.selectedPlayerBName);

        // Mines count based on difficulty
        int mines = getMinesForDifficulty(GameSetupController.selectedDifficulty);
        playerAMinesLabel.setText("💣 " + mines);
        playerBMinesLabel.setText("💣 " + mines);

        // Initial top bar values
        livesLabel.setText(lives + " / 10 ❤️");
        scoreLabel.setText("Score: " + score + " 🏆");

        // Board size according to difficulty
        int size = getBoardSize(GameSetupController.selectedDifficulty);

        // Build both boards with the same size
        buildBoardGrid(boardAGrid, size, true);   // golden style
        buildBoardGrid(boardBGrid, size, false);  // orange style
    }

    private int getBoardSize(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY -> 9;
            case MEDIUM -> 13;
            case HARD -> 16;
        };
    }

    private int getMinesForDifficulty(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY -> 10;
            case MEDIUM -> 26;
            case HARD -> 44;
        };
    }

    /**
     * Build a fixed-size grid of square buttons.
     * This is only UI – game logic can be added later.
     */
    private void buildBoardGrid(GridPane grid, int size, boolean isBoardA) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Button cell = new Button();

                cell.getStyleClass().add("cell-button");
                if (isBoardA) {
                    cell.getStyleClass().add("golden-cell");
                } else {
                    cell.getStyleClass().add("orange-cell");
                }

                // Make sure cell stays square with fixed size
                cell.setPrefSize(32, 32);
                cell.setMinSize(32, 32);
                cell.setMaxSize(32, 32);

                // TODO: later you can set onAction for game logic
                grid.add(cell, col, row);
            }
        }
    }

    @FXML
    private void onBackToHome(javafx.event.ActionEvent event) {
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
}
