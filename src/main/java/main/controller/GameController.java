package main.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import model.Difficulty;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.QuestionBank;
import model.QuestionLevel;
import model.ScoreRules;
import model.SurpriseType;
import javafx.scene.layout.HBox;
import model.Theme;


/**
 * Main controller for the game view. Handles UI initialization, game flow logic,
 * cell interactions, and transitions.
 */
public class GameController {

    @FXML private Label livesLabel;
    @FXML private Label scoreLabel;
    @FXML private Label playerANameLabel;
    @FXML private Label playerBNameLabel;
    @FXML private Label playerAMinesLabel;
    @FXML private Label playerBMinesLabel;
    @FXML private GridPane boardAGrid;
    @FXML private GridPane boardBGrid;
    @FXML private HBox heartsBox;


    // Game State Variables
    private int lives = 10;
    private int score = 0;
    private boolean isPlayerATurn = true;

    private final QuestionBank questionBank = new QuestionBank();
    private Theme playerATheme;
    private Theme playerBTheme;



    /**
     * Initializes the controller after its root element has been completely processed by the FXMLLoader.
     * Sets player names, initial lives, and builds the two game boards.
     */
    @FXML
    private void initialize() {
        // Set player names and mines based on setup
        playerANameLabel.setText(GameSetupController.selectedPlayerAName);
        playerBNameLabel.setText(GameSetupController.selectedPlayerBName);

        playerATheme = GameSetupController.selectedThemeA;
        playerBTheme = GameSetupController.selectedThemeB;

        int mines = getMinesForDifficulty(GameSetupController.selectedDifficulty);
        playerAMinesLabel.setText(String.valueOf(mines));
        playerBMinesLabel.setText(String.valueOf(mines));

        // Initialize game state based on difficulty model
        model.Difficulty diff = DifficultyMapper.toModel(GameSetupController.selectedDifficulty);

        lives = diff.getInitialLives();
        score = 0;

        // Build UI elements
        buildHearts(diff);
        updateLivesUI(diff);

        // Calculate board parameters
        int size = getBoardSize(GameSetupController.selectedDifficulty);
        int cellSize = getCellSize(GameSetupController.selectedDifficulty);

        // Building the game boards
        buildBoardGrid(boardAGrid, size, cellSize, true);
        buildBoardGrid(boardBGrid, size, cellSize, false);
        updateBoardHighlight();
    }


    /**
     * Updates the visual style of the boards (active/inactive) based on the current player's turn.
     */
    private void updateBoardHighlight() {
        if (isPlayerATurn) {

            // Highlight Player A's board
            boardAGrid.getStyleClass().add("active-board");
            boardAGrid.getStyleClass().remove("inactive-board");

            boardBGrid.getStyleClass().add("inactive-board");
            boardBGrid.getStyleClass().remove("active-board");
        } else {
            // Highlight Player B's board
            boardBGrid.getStyleClass().add("active-board");
            boardBGrid.getStyleClass().remove("inactive-board");

            boardAGrid.getStyleClass().add("inactive-board");
            boardAGrid.getStyleClass().remove("active-board");
        }
    }
    private void handleCellClick(Button cell, boolean isBoardA) {

        if (isPlayerATurn && !isBoardA) return;
        if (!isPlayerATurn && isBoardA) return;
        System.out.println("Player clicked on: " + (isBoardA ? "A" : "B"));
        isPlayerATurn = !isPlayerATurn;
        updateBoardHighlight();
    }

    private void toggleFlag(Button cell) {
        String current = cell.getText();

        if ("🐾".equals(current)) {
            cell.setText("");
            cell.getStyleClass().remove("paw-flag");
        } else {
            cell.setText("🐾");
            if (!cell.getStyleClass().contains("paw-flag")) {
                cell.getStyleClass().add("paw-flag");
            }
        }
    }

    private void buildHearts(model.Difficulty diff) {
        heartsBox.getChildren().clear();

        int total = diff.getInitialLives();

        for (int i = 0; i < total; i++) {
            Label heart = new Label("❤");
            heart.getStyleClass().add("heart-icon");
            heartsBox.getChildren().add(heart);
        }
    }

    private void updateLivesUI(model.Difficulty diff) {
        livesLabel.setText(lives + " / " + diff.getInitialLives());

        int total = diff.getInitialLives();

        for (int i = 0; i < heartsBox.getChildren().size(); i++) {
            Node node = heartsBox.getChildren().get(i);
            if (node instanceof Label heart) {
                if (i < lives) {
                    heart.getStyleClass().remove("heart-lost");
                } else {
                    if (!heart.getStyleClass().contains("heart-lost")) {
                        heart.getStyleClass().add("heart-lost");
                    }
                }
            }
        }
    }

    private int getBoardSize(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY   -> 9;
            case MEDIUM -> 13;
            case HARD   -> 16;
        };
    }

    // גודל המשבצת – מותאם כך שכל הלוח יישב נוח במסך
    private int getCellSize(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY   -> 44;
            case MEDIUM -> 36;
            case HARD   -> 28;
        };
    }

    private int getMinesForDifficulty(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY   -> 10;
            case MEDIUM -> 26;
            case HARD   -> 44;
        };
    }

    private QuestionLevel getLevelFromSetup() {
        GameSetupController.Difficulty d = GameSetupController.selectedDifficulty;

        return switch (d) {
            case EASY -> QuestionLevel.EASY;
            case MEDIUM -> QuestionLevel.MEDIUM;
            case HARD -> QuestionLevel.HARD;
        };
    }

    private void triggerRandomSurprise() {
        Difficulty diff = DifficultyMapper.toModel(GameSetupController.selectedDifficulty);
        boolean good = Math.random() < 0.5;
        SurpriseType type = good ? SurpriseType.GOOD : SurpriseType.BAD;

        ScoreRules.ScoreChange change = ScoreRules.surpriseTriggered(diff, type);

        lives += change.getLivesDelta();
        if (lives < 0) lives = 0;

        score += change.getPointsDelta();
        scoreLabel.setText("Score: " + score);

        updateLivesUI(diff);
        showSurprisePopup(change);
    }

    private void showSurprisePopup(ScoreRules.ScoreChange change) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/surprise-view.fxml")
            );
            Parent root = loader.load();

            SurpriseController controller = loader.getController();
            controller.setData(change);

            Stage dialog = new Stage();
            dialog.initOwner(scoreLabel.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Builds a fixed-size game board that looks visually balanced on the screen.
     */
    private void buildBoardGrid(GridPane grid, int size, int cellSize, boolean isBoardA) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Button cell = new Button();
                cell.getStyleClass().add("cell-button");

                Theme theme = isBoardA ? playerATheme : playerBTheme;

                if (theme != null) {
                    cell.setStyle(
                            theme.cellStyle +
                                    " -fx-background-radius: 8; -fx-border-radius: 8;"
                    );
                }

                cell.setOnAction(e -> handleCellClick(cell, isBoardA));

                cell.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        toggleFlag(cell);
                    }
                });

                // Fixed cell size per difficulty, not affected by resizing
                cell.setPrefSize(cellSize, cellSize);
                cell.setMinSize(cellSize, cellSize);
                cell.setMaxSize(cellSize, cellSize);

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
