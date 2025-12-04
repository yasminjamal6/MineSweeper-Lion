package main.controller;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
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
import model.Board;
import model.Cell;
import model.RevealResult;

import java.io.InputStream;


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
    private Board boardA;
    private Board boardB;
    @FXML private StackPane boardAContainer;
    @FXML private StackPane boardBContainer;
    @FXML private AnchorPane root;


    // Game State Variables
    private int lives = 10;
    private int score = 0;
    private boolean isPlayerATurn = true;

    private final QuestionBank questionBank = new QuestionBank();
    private Theme playerATheme;
    private Theme playerBTheme;

    // Resources
    private Image mineImage;
    private double mineImageSize;


    /**
     * Initializes the controller after its root element has been completely processed by the FXMLLoader.
     * Sets player names, initial lives, and builds the two game boards.
     */
    @FXML
    private void initialize() {
        try {
            InputStream is = getClass().getResourceAsStream("/images/bomb2.png");
            if (is != null) {
                mineImage = new Image(is);
            } else {
                System.err.println("Could not load /images/bomb2.png. Using fallback.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        playerANameLabel.setText(GameSetupController.selectedPlayerAName);
        playerBNameLabel.setText(GameSetupController.selectedPlayerBName);

        playerATheme = GameSetupController.selectedThemeA;
        playerBTheme = GameSetupController.selectedThemeB;

        int mines = getMinesForDifficulty(GameSetupController.selectedDifficulty);
        playerAMinesLabel.setText(String.valueOf(mines));
        playerBMinesLabel.setText(String.valueOf(mines));

        model.Difficulty diff = DifficultyMapper.toModel(GameSetupController.selectedDifficulty);
        lives = diff.getInitialLives();
        score = 0;

        buildHearts(diff);
        updateLivesUI(diff);
        scoreLabel.setText("Score: " + score + " 🏆");

        int size = getBoardSize(GameSetupController.selectedDifficulty);
        int cellSize = getCellSize(GameSetupController.selectedDifficulty);

        this.mineImageSize = cellSize * 0.70;

        boardA = new Board(size, size, playerATheme);
        boardB = new Board(size, size, playerBTheme);

        boardA.generate(diff);
        boardB.generate(diff);

        buildBoardGrid(boardAGrid, size, cellSize, true);
        buildBoardGrid(boardBGrid, size, cellSize, false);

        boardAContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.48));
        boardBContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.48));

        boardAContainer.prefHeightProperty().bind(root.heightProperty().multiply(0.72));
        boardBContainer.prefHeightProperty().bind(root.heightProperty().multiply(0.72));

        boardAGrid.prefWidthProperty().bind(boardAContainer.widthProperty().subtract(44));
        boardAGrid.prefHeightProperty().bind(boardAContainer.heightProperty().subtract(44));

        boardBGrid.prefWidthProperty().bind(boardBContainer.widthProperty().subtract(44));
        boardBGrid.prefHeightProperty().bind(boardBContainer.heightProperty().subtract(44));

        updateBoardHighlight();
    }


    /**
     * Updates the visual style of the boards (active/inactive) based on the current player's turn.
     */
    private void updateBoardHighlight() {
        if (isPlayerATurn) {
            boardAContainer.getStyleClass().add("active-board");
            boardAContainer.getStyleClass().remove("inactive-board");

            boardBContainer.getStyleClass().add("inactive-board");
            boardBContainer.getStyleClass().remove("active-board");
        } else {
            boardBContainer.getStyleClass().add("active-board");
            boardBContainer.getStyleClass().remove("inactive-board");

            boardAContainer.getStyleClass().add("inactive-board");
            boardAContainer.getStyleClass().remove("active-board");
        }
    }

    /**
     * Handles a left-click on a cell button.
     * Connects the UI button to the underlying board cell and updates the view.
     */
    private void handleCellClick(Button cellButton, boolean isBoardA, int row, int col) {

        // Enforce turn: Player A can only click on board A, Player B only on board B
        if (isPlayerATurn && !isBoardA) return;
        if (!isPlayerATurn && isBoardA) return;

        System.out.println("Player clicked on: " + (isBoardA ? "A" : "B") +
                " at (" + row + "," + col + ")");

        // Get the correct board (A or B)
        Board board = isBoardA ? boardA : boardB;

        // Reveal the cell in the model
        RevealResult result = board.revealCell(row, col);

        // Update the UI for this specific cell
        updateCellView(board, cellButton, row, col);
        refreshEntireBoard(board, isBoardA ? boardAGrid : boardBGrid);

        // TODO: handle score / lives / surprises based on the result if needed
        // e.g. if (result == RevealResult.HIT_MINE) { ... }

        // Switch turn after a valid click
        isPlayerATurn = !isPlayerATurn;
        updateBoardHighlight();
    }
    /**
     * Updates the visual state of a single cell button based on the underlying Cell model.
     * * @param board
     * @param cellButton
     * @param row
     * @param col
     */
    private void updateCellView(Board board, Button cellButton, int row, int col) {
        Cell cell = board.getCell(row, col);

        cellButton.setStyle(null);

        if (!cell.isRevealed()) {
            cellButton.setText("");
            cellButton.setGraphic(null);
            return;
        }

        cellButton.getStyleClass().removeIf(
                s -> s.startsWith("number-") || s.equals("mine-icon")
        );

        if (!cellButton.getStyleClass().contains("cell-revealed")) {
            cellButton.getStyleClass().add("cell-revealed");
        }

        if (cell.isMine()) {
            cellButton.setText("");

            if (mineImage != null) {
                ImageView iv = new ImageView(mineImage);
                iv.setFitWidth(mineImageSize);
                iv.setFitHeight(mineImageSize);
                iv.setPreserveRatio(true);
                cellButton.setGraphic(iv);
            } else {
                // Fallback if image not found
                cellButton.setText("💣");
            }

            if (!cellButton.getStyleClass().contains("mine-icon")) {
                cellButton.getStyleClass().add("mine-icon");
            }


            cellButton.setStyle("-fx-padding: 0;");

        } else {
            cellButton.setGraphic(null);
            int num = cell.getAdjacentMines();
            if (num == 0) {
                cellButton.setText("");
            } else {
                cellButton.setText(String.valueOf(num));
                String cls = "number-" + num;
                if (!cellButton.getStyleClass().contains(cls)) {
                    cellButton.getStyleClass().add(cls);
                }
            }
            cellButton.setStyle(null);
        }

        cellButton.setDisable(true);
        // Workaround for disabled buttons opacity in standard JavaFX modena (optional)
        if (cellButton.getStyle() == null || !cellButton.getStyle().contains("-fx-opacity")) {
            cellButton.setStyle((cellButton.getStyle() == null ? "" : cellButton.getStyle()) + "-fx-opacity: 1.0;");
        }
    }

    private void refreshEntireBoard(Board board, GridPane grid) {
        for (Node node : grid.getChildren()) {
            if (node instanceof Button btn) {
                int col = GridPane.getColumnIndex(btn);
                int row = GridPane.getRowIndex(btn);

                Cell cell = board.getCell(row, col);

                if (cell.isRevealed()) {
                    updateCellView(board, btn, row, col);
                }
            }
        }
    }


    private void toggleFlag(Button cell) {
        if (cell.isDisabled()) return;

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

        // מחיקה של לבבות קודמים
        heartsBox.getChildren().clear();

        // מרווח שלילי כדי לצמצם אותם ממש
        heartsBox.setSpacing(-40);  // 👈 זה מה שרצית

        int total = diff.getInitialLives();

        // טוענים את תמונת הלב
        Image heartImg = new Image(
                getClass().getResourceAsStream("/images/heart.png")
        );

        for (int i = 0; i < total; i++) {

            ImageView heart = new ImageView(heartImg);
            heart.setFitWidth(100);     // 👈 הגודל הגדול שביקשת
            heart.setFitHeight(100);
            heart.setPreserveRatio(true);

            // שמירה על אותו CSS class (אל תשני שם!)
            heart.getStyleClass().add("heart-icon");

            // ביטול מרווח טבעי שה-JavaFX מוסיף
            HBox.setMargin(heart, new Insets(0));

            // אפקט נשימה (אנימציה)
            FadeTransition ft = new FadeTransition(Duration.millis(1200), heart);
            ft.setFromValue(1.0);
            ft.setToValue(0.65);
            ft.setCycleCount(Animation.INDEFINITE);
            ft.setAutoReverse(true);
            ft.setDelay(Duration.millis(i * 120));
            ft.play();

            // הוספה ל-HBox
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
            case EASY    -> 9;
            case MEDIUM -> 13;
            case HARD    -> 16;
        };
    }

    private int getCellSize(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY    -> 44;
            case MEDIUM -> 36;
            case HARD    -> 28;
        };
    }

    private int getMinesForDifficulty(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY    -> 10;
            case MEDIUM -> 26;
            case HARD    -> 44;
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

        for (int i = 0; i < size; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / size);
            grid.getColumnConstraints().add(colConst);

            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / size);
            grid.getRowConstraints().add(rowConst);
        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Button cell = new Button();
                cell.getStyleClass().add("cell-button");
                cell.setMnemonicParsing(false);

                Theme theme = isBoardA ? playerATheme : playerBTheme;

                if (theme != null && theme.cellStyle != null) {
                    String themeStyle = theme.cellStyle;
                    cell.setStyle(themeStyle + " -fx-background-radius: 8; -fx-border-radius: 8;");
                }


                final int rowIndex = row;
                final int colIndex = col;

                cell.setOnAction(e -> handleCellClick(cell, isBoardA, rowIndex, colIndex));

                cell.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        toggleFlag(cell);
                    }
                });


                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

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