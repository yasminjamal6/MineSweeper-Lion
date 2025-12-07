package main.controller;

import model.CellType;
import model.Question;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.stage.StageStyle;

import java.io.InputStream;

/**
 * Main controller for the game view.
 * Handles UI initialization, game flow logic, cell interactions, and transitions.
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
    @FXML private StackPane boardAContainer;
    @FXML private StackPane boardBContainer;
    @FXML private AnchorPane root;
    @FXML private StackPane countdownOverlay;
    @FXML private Label countdownLabel;
    @FXML private Label timerLabel;

    private Board boardA;
    private Board boardB;

    // Game State
    private int lives = 10;
    private int score = 0;
    private boolean isPlayerATurn = true;

    private final QuestionBank questionBank = new QuestionBank();
    private Theme playerATheme;
    private Theme playerBTheme;

    // Resources
    private Image mineImage;
    private double mineImageSize;

    // Heart images (full + broken)
    private Image fullHeartImage;
    private Image emptyHeartImage;

    // Keep difficulty for lives updates
    private model.Difficulty currentDifficulty;
    private Timeline timerTimeline;
    private long timerStartMillis;
    private Image openGiftImage;

    // -------------------------------------------------------------------------
    // INITIALIZE
    // -------------------------------------------------------------------------

    @FXML
    private void initialize() {
        // Load mine image
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

        // Load heart images (full + broken)
        try {
            InputStream full = getClass().getResourceAsStream("/images/life.png");
            if (full != null) {
                fullHeartImage = new Image(full);
            } else {
                System.err.println("Could not load /images/life.png");
            }

            InputStream empty = getClass().getResourceAsStream("/images/no_life.png");
            if (empty != null) {
                emptyHeartImage = new Image(empty);
            } else {
                System.err.println("Could not load /images/no_life.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load open gift image
        try {
            InputStream openGift = getClass().getResourceAsStream("/images/openGift.png");
            if (openGift != null) {
                openGiftImage = new Image(openGift);
            } else {
                System.err.println("Could not load /images/openGift.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Player names and themes
        playerANameLabel.setText(GameSetupController.selectedPlayerAName);
        playerBNameLabel.setText(GameSetupController.selectedPlayerBName);

        playerATheme = GameSetupController.selectedThemeA;
        playerBTheme = GameSetupController.selectedThemeB;

        // Mines count per difficulty
        int mines = getMinesForDifficulty(GameSetupController.selectedDifficulty);
        playerAMinesLabel.setText(String.valueOf(mines));
        playerBMinesLabel.setText(String.valueOf(mines));

        // Difficulty and initial lives
        currentDifficulty = DifficultyMapper.toModel(GameSetupController.selectedDifficulty);
        lives = currentDifficulty.getInitialLives();
        score = 0;

        // Hearts bar + lives label
        buildHearts(currentDifficulty);
        updateLivesUI(currentDifficulty);
        scoreLabel.setText("Score: " + score + " 🏆");

        // Board size & cell size
        int size = getBoardSize(GameSetupController.selectedDifficulty);
        int cellSize = getCellSize(GameSetupController.selectedDifficulty);
        this.mineImageSize = cellSize * 0.70;

        // Boards
        boardA = new Board(size, size, playerATheme);
        boardB = new Board(size, size, playerBTheme);

        boardA.generate(currentDifficulty);
        boardB.generate(currentDifficulty);
        int questionCells = getQuestionCountForDifficulty(GameSetupController.selectedDifficulty);
        boardA.placeQuestionCells(questionCells);
        boardB.placeQuestionCells(questionCells);

        int surpriseCells = currentDifficulty.getSurpriseCells();
        boardA.placeSurpriseCells(surpriseCells);
        boardB.placeSurpriseCells(surpriseCells);

        // Build board grids
        buildBoardGrid(boardAGrid, size, cellSize, true);
        buildBoardGrid(boardBGrid, size, cellSize, false);

        // Responsive containers
        boardAContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.48));
        boardBContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.48));

        boardAContainer.prefHeightProperty().bind(root.heightProperty().multiply(0.72));
        boardBContainer.prefHeightProperty().bind(root.heightProperty().multiply(0.72));

        boardAGrid.prefWidthProperty().bind(boardAContainer.widthProperty().subtract(44));
        boardAGrid.prefHeightProperty().bind(boardAContainer.heightProperty().subtract(44));

        boardBGrid.prefWidthProperty().bind(boardBContainer.widthProperty().subtract(44));
        boardBGrid.prefHeightProperty().bind(boardBContainer.heightProperty().subtract(44));

        updateBoardHighlight();

        // Show countdown overlay before play begins
        startCountdown();
    }

    // -------------------------------------------------------------------------
    // TURN HIGHLIGHT + COUNTDOWN + TIMER
    // -------------------------------------------------------------------------

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

    private void startCountdown() {
        if (countdownOverlay == null || countdownLabel == null) {
            return;
        }
        countdownOverlay.setVisible(true);
        countdownOverlay.setMouseTransparent(false);
        countdownOverlay.toFront();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> countdownLabel.setText("3")),
                new KeyFrame(Duration.seconds(1), e -> countdownLabel.setText("2")),
                new KeyFrame(Duration.seconds(2), e -> countdownLabel.setText("1")),
                new KeyFrame(Duration.seconds(3), e -> countdownLabel.setText("Let's start!")),
                new KeyFrame(Duration.seconds(3.7), e -> {
                    countdownOverlay.setVisible(false);
                    countdownOverlay.setMouseTransparent(true);
                    startTimer();
                })
        );
        timeline.play();
    }

    private void startTimer() {
        if (timerLabel == null) {
            return;
        }

        timerStartMillis = System.currentTimeMillis();
        timerLabel.setText("00:00");

        if (timerTimeline != null) {
            timerTimeline.stop();
        }

        timerTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateTimerLabel()),
                new KeyFrame(Duration.seconds(1))
        );
        timerTimeline.setCycleCount(Animation.INDEFINITE);
        timerTimeline.play();
    }

    private void updateTimerLabel() {
        long elapsedMillis = System.currentTimeMillis() - timerStartMillis;
        long totalSeconds = elapsedMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    // -------------------------------------------------------------------------
    // CELL CLICK / GAME FLOW
    // -------------------------------------------------------------------------

    private void handleCellClick(Button cellButton, boolean isBoardA, int row, int col) {
        // אם אין לבבות – לא ממשיכים לשחק
        if (lives <= 0) {
            System.out.println("No hearts left – click ignored.");
            return;
        }

        if (isPlayerATurn && !isBoardA) return;
        if (!isPlayerATurn && isBoardA) return;

        System.out.println("Player clicked on: " + (isBoardA ? "A" : "B") +
                " at (" + row + "," + col + ")");

        Board board = isBoardA ? boardA : boardB;
        Cell cell  = board.getCell(row, col);

        // SURPRISE that is already revealed but not used yet
        if (cell.isRevealed()
                && cell.getType() == CellType.SURPRISE
                && !cell.isSurpriseUsed()) {

            handleSurpriseActivation(cell, cellButton);

            isPlayerATurn = !isPlayerATurn;
            updateBoardHighlight();
            checkGameOver();
            return;
        }

        // QUESTION that is already revealed
        if (cell.isRevealed()
                && cell.getType() == CellType.QUESTION) {

            if (!cell.isQuestionUsed()) {
                handleQuestionCell(board, row, col, cellButton);
            }

            isPlayerATurn = !isPlayerATurn;
            updateBoardHighlight();
            checkGameOver();
            return;
        }

        RevealResult result = board.revealCell(row, col);
        updateCellView(board, cellButton, row, col);
        refreshEntireBoard(board, isBoardA ? boardAGrid : boardBGrid);

        if (result == RevealResult.QUESTION_CELL) {
            if (!cell.isQuestionUsed()) {
                handleQuestionCell(board, row, col, cellButton);
            }
        }
        if (result == RevealResult.HIT_MINE) {
            lives--;
            if (lives < 0) lives = 0;
            updateLivesUI(currentDifficulty);
        }

        isPlayerATurn = !isPlayerATurn;
        updateBoardHighlight();

        // בדיקת סוף משחק אחרי כל מהלך
        checkGameOver();
    }

    private void handleSurpriseActivation(Cell cell, Button cellButton) {
        if (cell.isSurpriseUsed()) {
            return;
        }
        triggerRandomSurprise();
        cell.setSurpriseUsed(true);

        cellButton.setText("");
        if (openGiftImage != null) {
            ImageView iv = new ImageView(openGiftImage);
            iv.setFitWidth(32);
            iv.setFitHeight(32);
            iv.setPreserveRatio(true);
            cellButton.setGraphic(iv);
        } else {
            cellButton.setGraphic(null);
            cellButton.setText("🎁");
        }
        cellButton.setDisable(true);
    }

    // -------------------------------------------------------------------------
    // QUESTION CELLS
    // -------------------------------------------------------------------------

    private void handleQuestionCell(Board board, int row, int col, Button cellButton) {
        System.out.println(">>> [handleQuestionCell] QUESTION cell at (" + row + "," + col + ")");

        Cell cell = board.getCell(row, col);

        if (cell.isQuestionUsed()) {
            System.out.println(">>> Question already USED – ignoring click");
            return;
        }

        if (!cell.hasQuestion()) {
            QuestionLevel level = getLevelFromSetup();
            Question question = questionBank.getRandomQuestion(level);
            cell.setQuestion(question);
        }

        cellButton.setGraphic(null);
        cellButton.setText("?");
        if (!cellButton.getStyleClass().contains("question-cell")) {
            cellButton.getStyleClass().add("question-cell");
        }

        Question question = cell.getQuestion();
        QuestionController controller = showQuestionPopup(question);

        if (controller != null) {
            boolean correct  = controller.isAnsweredCorrect();
            boolean answered = controller.wasAnswered();
            System.out.println(">>> Question result: answered=" + answered + ", correct=" + correct);

            if (answered) {
                cell.setQuestionUsed(true);

                cellButton.setText("?");
                cellButton.getStyleClass().remove("question-cell");
                if (!cellButton.getStyleClass().contains("question-used-cell")) {
                    cellButton.getStyleClass().add("question-used-cell");
                }
                cellButton.setDisable(true);
            }
        }
    }

    private QuestionController showQuestionPopup(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/question-view.fxml")
            );
            Parent root = loader.load();

            QuestionController controller = loader.getController();
            controller.setQuestion(question);

            Stage dialog = new Stage();
            dialog.initOwner(scoreLabel.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setTitle("שאלת טריוויה");

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();

            return controller;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // CELL UI UPDATE
    // -------------------------------------------------------------------------

    private void updateCellView(Board board, Button cellButton, int row, int col) {
        Cell cell = board.getCell(row, col);

        cellButton.setStyle(null);

        if (!cell.isRevealed()) {
            cellButton.setText("");
            cellButton.setGraphic(null);
            return;
        }

        cellButton.getStyleClass().removeIf(
                s -> s.startsWith("number-")
                        || s.equals("mine-icon")
                        || s.equals("question-cell")
                        || s.equals("question-used-cell")
                        || s.equals("surprise-cell")
                        || s.equals("surprise-used")
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
                cellButton.setText("💣");
            }

            if (!cellButton.getStyleClass().contains("mine-icon")) {
                cellButton.getStyleClass().add("mine-icon");
            }

            cellButton.setStyle("-fx-padding: 0;");
        }

        else if (cell.getType() == CellType.SURPRISE) {

            if (cell.isSurpriseUsed()) {
                cellButton.setText("");
                if (openGiftImage != null) {
                    ImageView iv = new ImageView(openGiftImage);
                    iv.setFitWidth(32);
                    iv.setFitHeight(32);
                    iv.setPreserveRatio(true);
                    cellButton.setGraphic(iv);
                } else {
                    cellButton.setGraphic(null);
                    cellButton.setText("🎁");
                }

                cellButton.getStyleClass().remove("surprise-cell");
                if (!cellButton.getStyleClass().contains("surprise-used")) {
                    cellButton.getStyleClass().add("surprise-used");
                }
            } else {
                cellButton.setGraphic(null);
                cellButton.setText("🎁");
                cellButton.getStyleClass().remove("surprise-used");
                if (!cellButton.getStyleClass().contains("surprise-cell")) {
                    cellButton.getStyleClass().add("surprise-cell");
                }
            }

            cellButton.setStyle(null);
        }

        else if (cell.getType() == CellType.QUESTION) {
            cellButton.setGraphic(null);
            cellButton.setText("?");

            if (!cell.isQuestionUsed()) {
                if (!cellButton.getStyleClass().contains("question-cell")) {
                    cellButton.getStyleClass().add("question-cell");
                }
            } else {
                if (!cellButton.getStyleClass().contains("question-used-cell")) {
                    cellButton.getStyleClass().add("question-used-cell");
                }
                cellButton.setDisable(true);
            }

            cellButton.setStyle(null);
        }

        else {
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

        if (cell.getType() == CellType.SURPRISE && !cell.isSurpriseUsed()) {
            cellButton.setDisable(false);
        } else if (cell.getType() == CellType.QUESTION) {
            // handled above
        } else {
            cellButton.setDisable(true);
        }

        if (cellButton.getStyle() == null || !cellButton.getStyle().contains("-fx-opacity")) {
            cellButton.setStyle(
                    (cellButton.getStyle() == null ? "" : cellButton.getStyle()) +
                            "-fx-opacity: 1.0;"
            );
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

                    if (cell.getType() == CellType.SURPRISE && !cell.isSurpriseUsed()) {
                        btn.setGraphic(null);
                        btn.setText("🎁");
                        if (!btn.getStyleClass().contains("surprise-cell")) {
                            btn.getStyleClass().add("surprise-cell");
                        }
                    }

                    if (cell.getType() == CellType.SURPRISE && cell.isSurpriseUsed()) {
                        btn.setText("");
                        if (openGiftImage != null) {
                            ImageView iv = new ImageView(openGiftImage);
                            iv.setFitWidth(32);
                            iv.setFitHeight(32);
                            iv.setPreserveRatio(true);
                            btn.setGraphic(iv);
                        } else {
                            btn.setGraphic(null);
                            btn.setText("🎁");
                        }

                        if (!btn.getStyleClass().contains("surprise-used")) {
                            btn.getStyleClass().add("surprise-used");
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // FLAG TOGGLE
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // HEART BAR
    // -------------------------------------------------------------------------

    private void buildHearts(model.Difficulty diff) {
        heartsBox.getChildren().clear();

        heartsBox.setSpacing(8);
        heartsBox.setPadding(new Insets(0));

        int total = diff.getInitialLives();

        for (int i = 0; i < total; i++) {
            Node heartNode;

            if (fullHeartImage != null) {
                ImageView heartView = new ImageView(fullHeartImage);
                heartView.setFitWidth(36);
                heartView.setFitHeight(36);
                heartView.setPreserveRatio(false);
                heartView.setSmooth(true);
                heartView.getStyleClass().add("heart-icon");
                heartNode = heartView;
            } else {
                Label heartLabel = new Label("❤");
                heartLabel.getStyleClass().add("heart-icon");
                heartLabel.setFont(Font.font(34));
                heartNode = heartLabel;
            }

            HBox.setMargin(heartNode, Insets.EMPTY);

            FadeTransition ft = new FadeTransition(Duration.millis(1200), heartNode);
            ft.setFromValue(1.0);
            ft.setToValue(0.65);
            ft.setCycleCount(Animation.INDEFINITE);
            ft.setAutoReverse(true);
            ft.setDelay(Duration.millis(i * 120));
            ft.play();

            heartsBox.getChildren().add(heartNode);
        }
    }

    private void updateLivesUI(model.Difficulty diff) {
        livesLabel.setText(lives + " / " + diff.getInitialLives());

        for (int i = 0; i < heartsBox.getChildren().size(); i++) {
            Node node = heartsBox.getChildren().get(i);

            if (node instanceof ImageView heartView) {
                if (i < lives) {
                    if (fullHeartImage != null) {
                        heartView.setImage(fullHeartImage);
                        heartView.setOpacity(1.0);
                    }
                } else {
                    if (emptyHeartImage != null) {
                        heartView.setImage(emptyHeartImage);
                        heartView.setOpacity(1.0);
                    } else if (fullHeartImage != null) {
                        heartView.setImage(fullHeartImage);
                        heartView.setOpacity(0.3);
                    }
                }
            } else if (node instanceof Label heartLabel) {
                if (i < lives) {
                    heartLabel.setText("❤");
                    heartLabel.setOpacity(1.0);
                } else {
                    heartLabel.setText("♡");
                    heartLabel.setOpacity(0.6);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // DIFFICULTY HELPERS
    // -------------------------------------------------------------------------

    private int getBoardSize(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY    -> 9;
            case MEDIUM  -> 13;
            case HARD    -> 16;
        };
    }

    private int getCellSize(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY    -> 44;
            case MEDIUM  -> 36;
            case HARD    -> 28;
        };
    }

    private int getMinesForDifficulty(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY    -> 10;
            case MEDIUM  -> 26;
            case HARD    -> 44;
        };
    }

    private int getQuestionCountForDifficulty(GameSetupController.Difficulty diff) {
        return switch (diff) {
            case EASY    -> 6;
            case MEDIUM  -> 7;
            case HARD    -> 11;
        };
    }

    private QuestionLevel getLevelFromSetup() {
        GameSetupController.Difficulty d = GameSetupController.selectedDifficulty;

        return switch (d) {
            case EASY   -> QuestionLevel.EASY;
            case MEDIUM -> QuestionLevel.MEDIUM;
            case HARD   -> QuestionLevel.HARD;
        };
    }

    // -------------------------------------------------------------------------
    // SURPRISE CELLS / SCORE
    // -------------------------------------------------------------------------

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

        // אם ההפתעה גמרה את כל הלבבות – מיד מסיימים משחק, בלי פופאפ
        checkGameOver();
        if (lives <= 0) {
            return;
        }

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

    // -------------------------------------------------------------------------
    // BUILD BOARD GRID
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // GAME OVER + RESULT SCREEN
    // -------------------------------------------------------------------------

    /** כמה נקודות שווה כל לב שנשאר בסוף המשחק לפי רמת קושי */
    private int getHeartToPointsValue(model.Difficulty diff) {
        return switch (diff) {
            case EASY   -> 5;
            case MEDIUM -> 8;
            case HARD   -> 12;
        };
    }

    /** האם כל המוקשים על לוח מסוים נחשפו */
    private boolean areAllMinesRevealed(Board board) {
        int rows = board.getRows();
        int cols = board.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isMine() && !cell.isRevealed()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the game should end:
     *  - LOSS : no hearts left
     *  - WIN  : all mines on both boards are revealed
     */
    private void checkGameOver() {
        boolean noHeartsLeft = (lives <= 0);

        boolean allMinesBoardA = areAllMinesRevealed(boardA);
        boolean allMinesBoardB = areAllMinesRevealed(boardB);
        boolean allMinesCleared = allMinesBoardA && allMinesBoardB;

        System.out.println(">>> checkGameOver: lives=" + lives +
                ", allMinesA=" + allMinesBoardA +
                ", allMinesB=" + allMinesBoardB);

        if (!noHeartsLeft && !allMinesCleared) {
            return;
        }

        // עצירת הטיימר
        if (timerTimeline != null) {
            timerTimeline.stop();
        }

        if (noHeartsLeft) {
            System.out.println(">>> GAME OVER: LOSE");
            openResultScreen(false);
        } else if (allMinesCleared) {
            System.out.println(">>> GAME OVER: WIN");
            openResultScreen(true);
        }
    }

    private void openResultScreen(boolean win) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/result-view.fxml")
            );
            Parent resultRoot = loader.load();
            ResultController controller = loader.getController();

            String playerA = playerANameLabel.getText();
            String playerB = playerBNameLabel.getText();

            int heartValue = getHeartToPointsValue(currentDifficulty);
            int baseScoreValue = score;

            if (win) {
                controller.initAsWin(playerA, playerB, baseScoreValue, lives, heartValue);
            } else {
                controller.initAsLose(playerA, playerB, baseScoreValue, lives, heartValue);
            }

            Stage dialog = new Stage();
            dialog.setTitle("Game Result");

            // הבעלים הוא חלון המשחק
            dialog.initOwner(root.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(resultRoot);
            dialog.setScene(scene);

            dialog.sizeToScene();
            dialog.setResizable(false);

            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // BACK TO HOME BUTTON
    // -------------------------------------------------------------------------

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
