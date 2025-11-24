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
import model.Difficulty;
import javafx.scene.layout.HBox;


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

    private int lives = 10;
    private int score = 0;

    @FXML
    private void initialize() {
        playerANameLabel.setText(GameSetupController.selectedPlayerAName);
        playerBNameLabel.setText(GameSetupController.selectedPlayerBName);

        int mines = getMinesForDifficulty(GameSetupController.selectedDifficulty);
        playerAMinesLabel.setText("💣 " + mines);
        playerBMinesLabel.setText("💣 " + mines);

        model.Difficulty diff = DifficultyMapper.toModel(GameSetupController.selectedDifficulty);

        lives = diff.getInitialLives();
        score = 0;

        buildHearts(diff);

        updateLivesUI(diff);

        int size = getBoardSize(GameSetupController.selectedDifficulty);
        int cellSize = getCellSize(GameSetupController.selectedDifficulty);

        buildBoardGrid(boardAGrid, size, cellSize, true);
        buildBoardGrid(boardBGrid, size, cellSize, false);
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

    private void loseLife(model.Difficulty diff) {
        if (lives > 0) {
            lives--;
            updateLivesUI(diff);
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
            case EASY   -> 44;  // לוח קטן – משבצת גדולה
            case MEDIUM -> 36;
            case HARD   -> 28;  // לוח גדול – משבצת קטנה כדי לא לגלוש
        };
    }

    private int getMinesForDifficulty(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY   -> 10;
            case MEDIUM -> 26;
            case HARD   -> 44;
        };
    }

    /**
     * בניית לוח בגודל קבוע שנראה טוב על המסך.
     */
    private void buildBoardGrid(GridPane grid, int size, int cellSize, boolean isBoardA) {
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

                // גודל ריבוע קבוע לפי קושי – לא משתנה עם Resize
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
