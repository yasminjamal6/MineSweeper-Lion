package main.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.scene.control.ContentDisplay;
import model.CellType;
import model.Question;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import model.GameHistory;
import model.GameHistoryManager;
import model.Difficulty;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.QuestionBank;
import model.QuestionLevel;
import model.ScoreRules;
import model.SurpriseType;
import javafx.scene.layout.HBox;
import model.Theme;
import model.ThemeColors;
import model.Board;
import model.Cell;
import model.RevealResult;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

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
    @FXML private StackPane resumeOverlay;
    @FXML private Label countdownLabel;
    @FXML private Label timerLabel;
    @FXML private Label turnALabel;
    @FXML private Label turnBLabel;
    @FXML private Button pauseBtn;

    // --- PAUSE STATE ---
    private boolean gamePaused = false;


    @FXML
    private void onPause() {

        // If already paused -> RESUME
        if (gamePaused) {
            resumeGame();
            return;
        }

        // Avoid pausing during countdown overlays
        if (countdownOverlay != null && countdownOverlay.isVisible()) return;
        if (resumeOverlay != null && resumeOverlay.isVisible()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Pause Game");
        alert.setHeaderText("Are you sure you want to pause the game?");
        alert.setContentText("Click on the screen (PAUSED) or press Pause again to resume.");

        ButtonType yes = new ButtonType("Yes, Pause");
        ButtonType no  = new ButtonType("No");
        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            pauseGame();
        }
    }


    private void pauseGame() {
        gamePaused = true;

        pauseTimer();
        stopCountdown();

        if (boardAGrid != null) boardAGrid.setDisable(true);
        if (boardBGrid != null) boardBGrid.setDisable(true);

        if (countdownOverlay != null && countdownLabel != null) {
            countdownLabel.setText("PAUSED\n\nClick anywhere to continue");
            countdownOverlay.setVisible(true);
            countdownOverlay.setMouseTransparent(false);
            countdownOverlay.toFront();
        }
    }


    private void resumeGame() {
        gamePaused = false;

        if (countdownOverlay != null) {
            countdownOverlay.setVisible(false);
            countdownOverlay.setMouseTransparent(true);
        }

        if (boardAGrid != null) boardAGrid.setDisable(false);
        if (boardBGrid != null) boardBGrid.setDisable(false);

        startTimer();
        updateBoardHighlight();
    }





    private Board boardA;
    private Board boardB;

    // to prevent farming +1 by flagging/unflagging the same mine
    private boolean[][] mineFlagRewardedA;
    private boolean[][] mineFlagRewardedB;
    private Timeline countdownTimeline;



    // Game State
    private int lives = 10;
    private int previousLives;    // כמה לבבות היו לפני העדכון האחרון
    private int score = 0;
    private boolean isPlayerATurn = true;
    private boolean historySaved = false;

    private final QuestionBank questionBank = new QuestionBank();
    private Theme playerATheme;
    private Theme playerBTheme;

    // Resources
    private Image mineImage;
    private double mineImageSize;

    // Heart images (full + broken)
    private Image fullHeartImage;
    private Image emptyHeartImage;
    private LocalDateTime startedAt;

    // Keep difficulty for lives updates
    private model.Difficulty currentDifficulty;
    private Timeline timerTimeline;
    private long timerStartMillis;
    private long timerElapsedMillis;
    private boolean timerRunning;
    private Image openGiftImage;
    private GameSaveData pendingSavedGame;

    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter SAVE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final double GIFT_ICON_SIZE = 18;

    private void setGiftOpenedGraphic(Button btn) {
        btn.setText("");
        btn.setGraphicTextGap(0);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        if (openGiftImage != null) {
            ImageView iv = new ImageView(openGiftImage);
            iv.setFitWidth(GIFT_ICON_SIZE);
            iv.setFitHeight(GIFT_ICON_SIZE);
            iv.setPreserveRatio(true);
            btn.setGraphic(iv);
        } else {
            btn.setGraphic(null);
            btn.setText("🎁");
            btn.setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    private void setGiftClosedText(Button btn) {
        btn.setGraphic(null);
        btn.setContentDisplay(ContentDisplay.TEXT_ONLY);
        btn.setGraphicTextGap(0);

        btn.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        btn.setEllipsisString("");

        btn.setText("🎁");

        if (GameSetupController.selectedDifficulty == GameSetupController.Difficulty.HARD) {
            btn.setStyle((btn.getStyle() == null ? "" : btn.getStyle()) + "-fx-font-size: 14px;");
        }
    }

    private void attachWindowHandlers() {
        if (root == null) {
            return;
        }
        Scene scene = root.getScene();
        if (scene == null) {
            return;
        }
        scene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
            if (newWindow != null) {
                Stage stage = (Stage) newWindow;
                stage.setOnHiding(e -> saveGameState(SaveStatus.IN_PROGRESS));
                stage.setOnCloseRequest(e -> saveGameState(SaveStatus.IN_PROGRESS));
            }
        });
        if (scene.getWindow() != null) {
            Stage stage = (Stage) scene.getWindow();
            stage.setOnHiding(e -> saveGameState(SaveStatus.IN_PROGRESS));
            stage.setOnCloseRequest(e -> saveGameState(SaveStatus.IN_PROGRESS));
        }
    }

    private void startNewGameFlow() {
        historySaved = false;
        timerElapsedMillis = 0;
        timerRunning = false;
        startedAt = LocalDateTime.now();
        isPlayerATurn = true;

        playerANameLabel.setText(GameSetupController.selectedPlayerAName);
        playerBNameLabel.setText(GameSetupController.selectedPlayerBName);

        playerATheme = GameSetupController.selectedThemeA;
        playerBTheme = GameSetupController.selectedThemeB;

        GameSetupController.Difficulty selectedDifficulty = GameSetupController.selectedDifficulty;

        int mines = getMinesForDifficulty(selectedDifficulty);
        playerAMinesLabel.setText(String.valueOf(mines));
        playerBMinesLabel.setText(String.valueOf(mines));

        currentDifficulty = DifficultyMapper.toModel(selectedDifficulty);
        lives = currentDifficulty.getInitialLives();
        previousLives = lives;
        score = 0;

        buildHearts(currentDifficulty);
        updateLivesUI(currentDifficulty);
        updateScoreLabel();
        installHeartsTooltip();

        int size = getBoardSize(selectedDifficulty);
        int cellSize = getCellSize(selectedDifficulty);

        mineFlagRewardedA = new boolean[size][size];
        mineFlagRewardedB = new boolean[size][size];

        this.mineImageSize = cellSize * 0.70;

        boardA = new Board(size, size, playerATheme);
        boardB = new Board(size, size, playerBTheme);

        boardA.generate(currentDifficulty);
        boardB.generate(currentDifficulty);
        int questionCells = getQuestionCountForDifficulty(selectedDifficulty);
        boardA.placeQuestionCells(questionCells);
        boardB.placeQuestionCells(questionCells);

        int surpriseCells = currentDifficulty.getSurpriseCells();
        boardA.placeSurpriseCells(surpriseCells);
        boardB.placeSurpriseCells(surpriseCells);

        buildBoardGrid(boardAGrid, size, cellSize, true);
        buildBoardGrid(boardBGrid, size, cellSize, false);
        updateMinesUI();

        applyLayoutBindings();
        updateBoardHighlight();

        boardAGrid.setMinSize(0, 0);
        boardBGrid.setMinSize(0, 0);

        startCountdown();
    }

    private void installHeartsTooltip() {
        Tooltip heartsTip = new Tooltip(
                "Hearts left. When they reach 0 – the hyenas take over!"
        );
        Tooltip.install(heartsBox, heartsTip);
    }

    private void applyLayoutBindings() {

        // Unbind first so Restart
        if (boardAContainer.prefWidthProperty().isBound()) boardAContainer.prefWidthProperty().unbind();
        if (boardBContainer.prefWidthProperty().isBound()) boardBContainer.prefWidthProperty().unbind();
        if (boardAContainer.prefHeightProperty().isBound()) boardAContainer.prefHeightProperty().unbind();
        if (boardBContainer.prefHeightProperty().isBound()) boardBContainer.prefHeightProperty().unbind();

        if (boardAGrid.prefWidthProperty().isBound()) boardAGrid.prefWidthProperty().unbind();
        if (boardAGrid.prefHeightProperty().isBound()) boardAGrid.prefHeightProperty().unbind();
        if (boardBGrid.prefWidthProperty().isBound()) boardBGrid.prefWidthProperty().unbind();
        if (boardBGrid.prefHeightProperty().isBound()) boardBGrid.prefHeightProperty().unbind();

        // Bind again
        boardAContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.48));
        boardBContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.48));

        boardAContainer.prefHeightProperty().bind(root.heightProperty().multiply(0.72));
        boardBContainer.prefHeightProperty().bind(root.heightProperty().multiply(0.72));

        boardAGrid.prefWidthProperty().bind(boardAContainer.widthProperty().subtract(44));
        boardAGrid.prefHeightProperty().bind(boardAContainer.heightProperty().subtract(44));

        boardBGrid.prefWidthProperty().bind(boardBContainer.widthProperty().subtract(44));
        boardBGrid.prefHeightProperty().bind(boardBContainer.heightProperty().subtract(44));
    }

    private void showResumeDialog(GameSaveData savedState) {
        pendingSavedGame = savedState;
        if (resumeOverlay == null) {
            resumeSavedGame(savedState);
            return;
        }
        resumeOverlay.setVisible(true);
        resumeOverlay.setMouseTransparent(false);
        resumeOverlay.toFront();
    }

    @FXML
    private void onResumeContinue() {
        if (pendingSavedGame != null) {
            resumeSavedGame(pendingSavedGame);
        } else {
            startNewGameFlow();
        }
        hideResumeOverlay();
    }

    @FXML
    private void onResumeNewGame() {
        clearSavedGame();
        hideResumeOverlay();
        startNewGameFlow();
    }

    private void hideResumeOverlay() {
        if (resumeOverlay != null) {
            resumeOverlay.setVisible(false);
            resumeOverlay.setMouseTransparent(true);
        }
    }

    private void resumeSavedGame(GameSaveData savedState) {
        try {
            GameSetupController.Difficulty savedDifficulty =
                    GameSetupController.Difficulty.valueOf(savedState.difficulty);

            GameSetupController.selectedDifficulty = savedDifficulty;
            GameSetupController.selectedPlayerAName = savedState.playerAName;
            GameSetupController.selectedPlayerBName = savedState.playerBName;

            playerATheme = findThemeById(savedState.playerAThemeId, GameSetupController.selectedThemeA);
            playerBTheme = findThemeById(savedState.playerBThemeId, GameSetupController.selectedThemeB);
            GameSetupController.selectedThemeA = playerATheme;
            GameSetupController.selectedThemeB = playerBTheme;

            playerANameLabel.setText(savedState.playerAName);
            playerBNameLabel.setText(savedState.playerBName);

            int mines = getMinesForDifficulty(savedDifficulty);
            playerAMinesLabel.setText(String.valueOf(mines));
            playerBMinesLabel.setText(String.valueOf(mines));

            currentDifficulty = DifficultyMapper.toModel(savedDifficulty);
            lives = savedState.lives;
            previousLives = lives;
            score = savedState.score;
            isPlayerATurn = savedState.isPlayerATurn;
            historySaved = false;
            startedAt = parseDate(savedState.startedAtIso).orElse(LocalDateTime.now());

            buildHearts(currentDifficulty);
            updateLivesUI(currentDifficulty);
            updateScoreLabel();
            installHeartsTooltip();

            int size = savedState.boardSize;
            int cellSize = getCellSize(savedDifficulty);
            this.mineImageSize = cellSize * 0.70;

            mineFlagRewardedA = ensureMineRewards(savedState.boardA != null ? savedState.boardA.mineFlagRewarded : null, size);
            mineFlagRewardedB = ensureMineRewards(savedState.boardB != null ? savedState.boardB.mineFlagRewarded : null, size);

            boardA = new Board(size, size, playerATheme);
            boardB = new Board(size, size, playerBTheme);

            applySavedBoard(boardA, savedState.boardA);
            applySavedBoard(boardB, savedState.boardB);

            buildBoardGrid(boardAGrid, size, cellSize, true);
            buildBoardGrid(boardBGrid, size, cellSize, false);
            applyLayoutBindings();

            boardAGrid.setMinSize(0, 0);
            boardBGrid.setMinSize(0, 0);

            applyBoardStateToUI(boardA, boardAGrid);
            applyBoardStateToUI(boardB, boardBGrid);
            updateMinesUI();
            updateBoardHighlight();

            timerElapsedMillis = Math.max(0, savedState.timerElapsedMillis);
            clearCountdownOverlay();
            startTimer();
        } catch (Exception e) {
            e.printStackTrace();
            startNewGameFlow();
        }
    }

    private void applySavedBoard(Board board, SavedBoard savedBoard) {
        if (board == null || savedBoard == null || savedBoard.cells == null) {
            return;
        }
        for (int r = 0; r < Math.min(board.getRows(), savedBoard.cells.length); r++) {
            SavedCell[] savedRow = savedBoard.cells[r];
            if (savedRow == null) {
                continue;
            }
            for (int c = 0; c < Math.min(board.getCols(), savedRow.length); c++) {
                SavedCell savedCell = savedRow[c];
                if (savedCell == null) {
                    continue;
                }
                Cell cell = board.getCell(r, c);

                if (savedCell.type == CellType.MINE) {
                    cell.setMine(true);
                } else if (savedCell.type != null) {
                    cell.setType(savedCell.type);
                }

                cell.setAdjacentMines(savedCell.adjacentMines);
                cell.setRevealed(savedCell.revealed);
                cell.setFlagged(savedCell.flagged);
                cell.setSurpriseUsed(savedCell.surpriseUsed);
                cell.setQuestionUsed(savedCell.questionUsed);
                if (savedCell.question != null) {
                    cell.setQuestion(savedCell.question);
                }
            }
        }
    }

    private boolean[][] ensureMineRewards(boolean[][] saved, int size) {
        if (saved != null && saved.length == size) {
            boolean valid = true;
            for (boolean[] row : saved) {
                if (row == null || row.length != size) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                return saved;
            }
        }
        return new boolean[size][size];
    }

    private void applyBoardStateToUI(Board board, GridPane grid) {
        for (Node node : grid.getChildren()) {
            if (node instanceof Button btn) {
                Integer col = GridPane.getColumnIndex(btn);
                Integer row = GridPane.getRowIndex(btn);
                if (col == null || row == null) continue;

                Cell cell = board.getCell(row, col);

                btn.getStyleClass().remove("paw-flag");
                btn.setText("");

                if (cell.isFlagged() && !cell.isRevealed()) {
                    btn.setText("🐾");
                    if (!btn.getStyleClass().contains("paw-flag")) {
                        btn.getStyleClass().add("paw-flag");
                    }
                }

                if (cell.isRevealed()) {
                    updateCellView(board, btn, row, col);

                    if (cell.getType() == CellType.SURPRISE && !cell.isSurpriseUsed()) {
                        setGiftClosedText(btn);
                        if (!btn.getStyleClass().contains("surprise-cell")) {
                            btn.getStyleClass().add("surprise-cell");
                        }
                    }

                    if (cell.getType() == CellType.SURPRISE && cell.isSurpriseUsed()) {
                        setGiftOpenedGraphic(btn);
                        if (!btn.getStyleClass().contains("surprise-used")) {
                            btn.getStyleClass().add("surprise-used");
                        }
                    }
                }
            }
        }
    }

    private void clearCountdownOverlay() {
        if (countdownOverlay != null) {
            countdownOverlay.setVisible(false);
            countdownOverlay.setMouseTransparent(true);
        }
    }

    private Theme findThemeById(String id, Theme fallback) {
        if (id != null) {
            for (Theme theme : ThemeColors.themes) {
                if (id.equals(theme.id)) {
                    return theme;
                }
            }
        }
        return fallback != null ? fallback : ThemeColors.themes.get(0);
    }

    private Optional<LocalDateTime> parseDate(String value) {
        try {
            if (value != null && !value.isBlank()) {
                return Optional.of(LocalDateTime.parse(value, SAVE_TIME_FORMAT));
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private void showResumePrompt(GameSaveData savedState) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resume Game");
        alert.setHeaderText("A previous game is in progress");
        alert.setContentText("Continue where you left off or start a fresh game?");

        ButtonType continueBtn = new ButtonType("Continue");
        ButtonType newGameBtn = new ButtonType("New Game");
        alert.getButtonTypes().setAll(continueBtn, newGameBtn);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/alert.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("resume-alert");

        if (root != null && root.getScene() != null) {
            alert.initOwner(root.getScene().getWindow());
        }
        alert.initModality(Modality.APPLICATION_MODAL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == continueBtn) {
            resumeSavedGame(savedState);
        } else {
            clearSavedGame();
            startNewGameFlow();
        }
    }

    // -------------------------------------------------------------------------
    // SAVE / LOAD
    // -------------------------------------------------------------------------

    private void saveGameState(SaveStatus status) {
        String key = currentSaveKey();
        if (key == null) {
            return;
        }

        if (historySaved || status == SaveStatus.COMPLETED || boardA == null || boardB == null || lives <= 0) {
            SavedGameRepository.delete(key);
            return;
        }

        pauseTimer();

        GameSaveData data = new GameSaveData();
        data.status = status;
        data.difficulty = GameSetupController.selectedDifficulty.name();
        data.boardSize = boardA.getRows();
        data.playerAName = playerANameLabel.getText();
        data.playerBName = playerBNameLabel.getText();
        data.playerAThemeId = playerATheme != null ? playerATheme.id : null;
        data.playerBThemeId = playerBTheme != null ? playerBTheme.id : null;
        data.isPlayerATurn = isPlayerATurn;
        data.lives = lives;
        data.score = score;
        data.timerElapsedMillis = timerElapsedMillis;
        data.startedAtIso = startedAt != null ? SAVE_TIME_FORMAT.format(startedAt) : null;
        data.lastUpdatedEpochMillis = System.currentTimeMillis();
        data.boardA = buildSavedBoard(boardA, mineFlagRewardedA, playerATheme);
        data.boardB = buildSavedBoard(boardB, mineFlagRewardedB, playerBTheme);

        SavedGameRepository.save(key, data);
    }

    private SavedBoard buildSavedBoard(Board board, boolean[][] mineRewarded, Theme theme) {
        SavedBoard saved = new SavedBoard();
        saved.themeId = theme != null ? theme.id : null;

        int rows = board.getRows();
        int cols = board.getCols();

        saved.cells = new SavedCell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                SavedCell sc = new SavedCell();

                sc.revealed = cell.isRevealed();
                sc.flagged = cell.isFlagged();
                sc.type = cell.getType();
                sc.adjacentMines = cell.getAdjacentMines();
                sc.surpriseUsed = cell.isSurpriseUsed();
                sc.questionUsed = cell.isQuestionUsed();
                sc.question = cell.getQuestion();

                saved.cells[r][c] = sc;
            }
        }

        if (mineRewarded != null && mineRewarded.length == rows) {
            saved.mineFlagRewarded = mineRewarded;
        } else {
            saved.mineFlagRewarded = new boolean[rows][cols];
        }
        return saved;
    }

    private GameSaveData loadGameState() {
        String key = currentSaveKey();
        if (key == null) return null;
        GameSaveData data = SavedGameRepository.loadLatest(key);
        if (data == null) {
            return null;
        }
        if (data.status == null || data.boardSize <= 0 || data.difficulty == null) {
            SavedGameRepository.delete(key);
            return null;
        }
        if (data.status == SaveStatus.COMPLETED) {
            return null;
        }
        return data;
    }

    private void clearSavedGame() {
        String key = currentSaveKey();
        if (key != null) {
            SavedGameRepository.delete(key);
        }
    }

    private String currentSaveKey() {
        String a = GameSetupController.selectedPlayerAName;
        String b = GameSetupController.selectedPlayerBName;
        GameSetupController.Difficulty diff = GameSetupController.selectedDifficulty;
        if (a == null || b == null || diff == null) {
            return null;
        }
        int size = getBoardSize(diff);
        return makeSaveKey(a, b, diff.name(), size);
    }

    private String makeSaveKey(String playerA, String playerB, String difficulty, int size) {
        String normA = normalizeName(playerA);
        String normB = normalizeName(playerB);
        String[] pair = new String[]{normA, normB};
        Arrays.sort(pair);
        String base = pair[0] + "_" + pair[1] + "_" + (difficulty == null ? "" : difficulty.toLowerCase()) + "_" + size;
        return base.replaceAll("[^a-z0-9_-]", "_");
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }


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
        Platform.runLater(() -> {
            attachWindowHandlers();
            GameSaveData savedState = loadGameState();
            if (savedState != null && savedState.status == SaveStatus.IN_PROGRESS) {
                if (GameSetupController.skipResumePrompt) {
                    GameSetupController.skipResumePrompt = false;
                    resumeSavedGame(savedState);
                    return;
                }
                showResumeDialog(savedState);
                return;
            }
            startNewGameFlow();
        });
        // Allow clicking the PAUSED overlay to resume the game
        if (countdownOverlay != null) {
            countdownOverlay.setOnMouseClicked(e -> {
                if (gamePaused) {
                    resumeGame();
                }
            });
        }

    }

    // -------------------------------------------------------------------------
    // TURN HIGHLIGHT + COUNTDOWN + TIMER
    // -------------------------------------------------------------------------

    private void updateBoardHighlight() {
        boardAContainer.getStyleClass().removeIf(s -> s.equals("active-board") || s.equals("inactive-board"));
        boardBContainer.getStyleClass().removeIf(s -> s.equals("active-board") || s.equals("inactive-board"));

        if (isPlayerATurn) {
            boardAContainer.getStyleClass().add("active-board");
            boardBContainer.getStyleClass().add("inactive-board");

            if (turnALabel != null && turnBLabel != null) {
                turnALabel.setText("👑 " + playerANameLabel.getText() + " – your turn");
                turnALabel.setVisible(true);
                turnBLabel.setVisible(false);
            }
        } else {
            boardBContainer.getStyleClass().add("active-board");
            boardAContainer.getStyleClass().add("inactive-board");

            if (turnALabel != null && turnBLabel != null) {
                turnBLabel.setText("🦁 " + playerBNameLabel.getText() + " – your turn");
                turnBLabel.setVisible(true);
                turnALabel.setVisible(false);
            }
        }

        System.out.println("A classes: " + boardAContainer.getStyleClass());
        System.out.println("B classes: " + boardBContainer.getStyleClass());
    }




    private void startCountdown() {
        if (countdownOverlay == null || countdownLabel == null) return;

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownOverlay.setVisible(true);
        countdownOverlay.setMouseTransparent(false);
        countdownOverlay.toFront();

        countdownTimeline = new Timeline(
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
        countdownTimeline.play();
    }
    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    @FXML
    private void onRestart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Circle of Life");
        alert.setHeaderText("Restart the Journey?");
        alert.setContentText(
                "The savanna will reset and your current adventure will be lost.\n\n" +
                        "Are you sure you want to begin a new journey?"
        );

        ButtonType restartBtn = new ButtonType("Restart");
        ButtonType cancelBtn  = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(restartBtn, cancelBtn);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/restart-confirm.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("restart-confirm");

        Button restartButton = (Button) dialogPane.lookupButton(restartBtn);
        Button cancelButton  = (Button) dialogPane.lookupButton(cancelBtn);

        restartButton.getStyleClass().add("restart-confirm-btn");
        cancelButton.getStyleClass().add("restart-cancel-btn");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != restartBtn) return;

        stopCountdown();
        pauseTimer();
        if (timerTimeline != null) {
            timerTimeline.stop();
            timerTimeline = null;
        }
        timerElapsedMillis = 0;
        timerRunning = false;
        clearSavedGame();
        hideResumeOverlay();
        clearCountdownOverlay();

        startNewGameFlow();
    }




    private void startTimer() {
        if (timerLabel == null) {
            return;
        }

        timerStartMillis = System.currentTimeMillis();
        timerRunning = true;

        if (timerTimeline != null) {
            timerTimeline.stop();
        }

        updateTimerLabel();

        timerTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateTimerLabel()),
                new KeyFrame(Duration.seconds(1))
        );
        timerTimeline.setCycleCount(Animation.INDEFINITE);
        timerTimeline.play();
    }

    private void updateTimerLabel() {
        long elapsedMillis = currentElapsedMillis();
        long totalSeconds = elapsedMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void pauseTimer() {
        timerElapsedMillis = currentElapsedMillis();
        if (timerTimeline != null) {
            timerTimeline.stop();
        }
        timerRunning = false;
    }

    private long currentElapsedMillis() {
        long base = timerElapsedMillis;
        if (timerRunning) {
            base += (System.currentTimeMillis() - timerStartMillis);
        }
        return base;
    }

    // -------------------------------------------------------------------------
    // CELL CLICK / GAME FLOW
    // -------------------------------------------------------------------------

    private void handleCellClick(Button cellButton, boolean isBoardA, int row, int col) {
        if (gamePaused) return;

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

        if (!cell.isRevealed() && (cell.isFlagged() || cellButton.getStyleClass().contains("paw-flag"))) {
            return;
        }

        int revealedBefore = countRevealed(board);

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
        updateMinesUI(isBoardA);

        int revealedAfter = countRevealed(board);
        int newlyRevealed = Math.max(0, revealedAfter - revealedBefore);


        if (result == RevealResult.HIT_MINE) {
            lives--;
            if (lives < 0) lives = 0;
            updateLivesUI(currentDifficulty);
        }
        if (newlyRevealed > 0 && result != RevealResult.HIT_MINE && result != RevealResult.IGNORED) {
            addScore(newlyRevealed);
        }

        isPlayerATurn = !isPlayerATurn;
        updateBoardHighlight();

        checkGameOver();
    }

    private void handleSurpriseActivation(Cell cell, Button cellButton) {
        if (cell.isSurpriseUsed()) {
            return;
        }
        triggerRandomSurprise();
        cell.setSurpriseUsed(true);

        setGiftOpenedGraphic(cellButton);
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
            Question question = questionBank.getRandomQuestionAnyLevel();
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
                    setGiftOpenedGraphic(cellButton);
                } else {
                    setGiftClosedText(cellButton);
                }

                cellButton.getStyleClass().remove("surprise-cell");
                if (!cellButton.getStyleClass().contains("surprise-used")) {
                    cellButton.getStyleClass().add("surprise-used");
                }
            } else {
                setGiftClosedText(cellButton);
                cellButton.getStyleClass().remove("surprise-used");
                if (!cellButton.getStyleClass().contains("surprise-cell")) {
                    cellButton.getStyleClass().add("surprise-cell");
                }
            }
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
                Integer col = GridPane.getColumnIndex(btn);
                Integer row = GridPane.getRowIndex(btn);
                if (col == null || row == null) continue;

                Cell cell = board.getCell(row, col);
                if (cell.isRevealed()) {
                    updateCellView(board, btn, row, col);

                    if (cell.getType() == CellType.SURPRISE && !cell.isSurpriseUsed()) {
                        setGiftClosedText(btn);
                        if (!btn.getStyleClass().contains("surprise-cell")) {
                            btn.getStyleClass().add("surprise-cell");
                        }
                    }

                    if (cell.getType() == CellType.SURPRISE && cell.isSurpriseUsed()) {
                        setGiftOpenedGraphic(btn);
                        if (!btn.getStyleClass().contains("surprise-used")) {
                            btn.getStyleClass().add("surprise-used");
                        }
                    }
                }
            }
        }
    }

    private void updateMinesUI() {
        updateMinesLabel(boardA, playerAMinesLabel);
        updateMinesLabel(boardB, playerBMinesLabel);
    }

    private void updateMinesUI(boolean isBoardA) {
        if (isBoardA) {
            updateMinesLabel(boardA, playerAMinesLabel);
        } else {
            updateMinesLabel(boardB, playerBMinesLabel);
        }
    }

    private void updateMinesLabel(Board board, Label label) {
        if (board == null || label == null) {
            return;
        }
        label.setText(String.valueOf(countRemainingMines(board)));
    }

    private int countRemainingMines(Board board) {
        if (board == null) {
            return 0;
        }
        int remaining = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isMine() && !cell.isRevealed()) {
                    remaining++;
                }
            }
        }
        return remaining;
    }

    // -------------------------------------------------------------------------
    // FLAG TOGGLE
    // -------------------------------------------------------------------------

    /**
     * Counts revealed cells on a board to support per-tile scoring.
     */
    private int countRevealed(Board board) {
        int count = 0;
        for (Cell[] row : board.getCells()) {
            for (Cell c : row) {
                if (c.isRevealed()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void toggleFlag(Button cellButton, boolean isBoardA, int row, int col) {

        if (isPlayerATurn && !isBoardA) return;
        if (!isPlayerATurn && isBoardA) return;
        if (gamePaused) return;

        Board board = isBoardA ? boardA : boardB;
        Cell cell = board.getCell(row, col);

        if (cellButton.isDisabled() || cell.isRevealed()) return;

        boolean flagged = cell.isFlagged() || cellButton.getStyleClass().contains("paw-flag");

        if (flagged) {
            cell.setFlagged(false);
            cellButton.setText("");
            cellButton.getStyleClass().remove("paw-flag");
        } else {
            cell.setFlagged(true);
            cellButton.setText("🐾");
            if (!cellButton.getStyleClass().contains("paw-flag")) {
                cellButton.getStyleClass().add("paw-flag");
            }

            if (cell.isMine()) {
                boolean[][] rewarded = isBoardA ? mineFlagRewardedA : mineFlagRewardedB;

                // +1 only once per mine cell
                if (!rewarded[row][col]) {
                    addScore(1);
                    rewarded[row][col] = true;
                }
            } else {
                addScore(-3);
            }
        }

        checkGameOver();
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

    private void updateScoreLabel() {
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score + " 🏆");
        }
    }

    private void addScore(int delta) {
        score = Math.max(0, score + delta);
        updateScoreLabel();
    }


    /**
     * Updates the hearts bar and lives label according to current lives.
     * Hearts before 'lives' = full, hearts after = broken/empty.
     */
    private void playHeartLostAnimation(Node node) {
        if (node == null) return;

        ScaleTransition st = new ScaleTransition(Duration.millis(220), node);
        st.setFromX(1.25);
        st.setFromY(1.25);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setCycleCount(1);
        st.play();
    }

    private void updateLivesUI(model.Difficulty diff) {
        livesLabel.setText(lives + " / " + diff.getInitialLives());

        // מצב קריטי – מעט לבבות
        if (lives <= 2) {
            if (!heartsBox.getStyleClass().contains("hearts-critical")) {
                heartsBox.getStyleClass().add("hearts-critical");
            }
        } else {
            heartsBox.getStyleClass().remove("hearts-critical");
        }

        for (int i = 0; i < heartsBox.getChildren().size(); i++) {
            Node node = heartsBox.getChildren().get(i);

            boolean wasFull = i < previousLives;
            boolean isFullNow = i < lives;

            // לב שעכשיו הפך ממלא לריק
            if (wasFull && !isFullNow) {
                playHeartLostAnimation(node);
            }

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

        // לזכור למהלך הבא
        previousLives = lives;
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

        addScore(change.getPointsDelta());
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

        grid.setHgap(0);
        grid.setVgap(0);
        grid.setPadding(Insets.EMPTY);


        for (int i = 0; i < size; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / size);
            colConst.setFillWidth(true);
            grid.getColumnConstraints().add(colConst);

            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / size);
            rowConst.setFillHeight(true);
            grid.getRowConstraints().add(rowConst);

        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Button cell = new Button();
                cell.getStyleClass().add("cell-button");
                cell.setMnemonicParsing(false);
                cell.setPadding(Insets.EMPTY);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                GridPane.setFillWidth(cell, true);
                GridPane.setFillHeight(cell, true);
                cell.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
                cell.setEllipsisString("");
                cell.setGraphicTextGap(0);

                Theme theme = isBoardA ? playerATheme : playerBTheme;

                if (theme != null && theme.cellStyle != null) {
                    String themeStyle = theme.cellStyle;
                    cell.setStyle(themeStyle + " -fx-background-radius: 8; -fx-border-radius: 8;");
                }

                final int rowIndex = row;
                final int colIndex = col;

                cell.setOnAction(e -> handleCellClick(cell, isBoardA, rowIndex, colIndex));

                cell.setOnMouseClicked(event -> {
                    boolean isRightClick = event.getButton() == MouseButton.SECONDARY;
                    boolean isMacCtrlClick = event.getButton() == MouseButton.PRIMARY && event.isControlDown();

                    if (isRightClick || isMacCtrlClick) {
                        toggleFlag(cell, isBoardA, rowIndex, colIndex);
                        event.consume();
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

    /** האם כל המוקשים על לוח מסוים נחשפו (דגלים על מוקשים נספרים כחשופים) */
    private boolean areAllMinesRevealed(Board board) {
        int rows = board.getRows();
        int cols = board.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isMine() && !(cell.isRevealed() || cell.isFlagged())) {
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
        boolean allMinesCleared = allMinesBoardA || allMinesBoardB;

        System.out.println(">>> checkGameOver: lives=" + lives +
                ", allMinesA=" + allMinesBoardA +
                ", allMinesB=" + allMinesBoardB);

        if (!noHeartsLeft && !allMinesCleared) {
            return;
        }

        pauseTimer();
        saveGameState(SaveStatus.COMPLETED);

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

            int heartsLeft = lives;
            int heartValue = getHeartToPointsValue(currentDifficulty);
            int baseScoreValue = score;
            int heartsBonus = win ? heartsLeft * heartValue : 0;

            if (win && heartsBonus != 0) {
                addScore(heartsBonus);
                lives = 0;
                updateLivesUI(currentDifficulty);
            }
            persistHistoryIfNeeded(win, heartsLeft);

            if (win) {
                controller.initAsWin(playerA, playerB, baseScoreValue, heartsLeft, heartValue);
            } else {
                controller.initAsLose(playerA, playerB, baseScoreValue, heartsLeft, heartValue);
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
        saveGameState(SaveStatus.IN_PROGRESS);
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
     * Records the current game outcome into the history manager.
     */
    private void saveGameHistory(int playerAHeartsLeft, int playerBHeartsLeft, boolean success) {
        try {
            String playerA = GameSetupController.selectedPlayerAName;
            String playerB = GameSetupController.selectedPlayerBName;
            Difficulty difficulty = currentDifficulty != null
                    ? currentDifficulty
                    : DifficultyMapper.toModel(GameSetupController.selectedDifficulty);

            LocalDateTime endedAt = LocalDateTime.now();
            int sharedLives = playerAHeartsLeft;

            GameHistory history = new GameHistory(
                    playerA,
                    playerB,
                    difficulty,
                    score,
                    sharedLives,
                    success,
                    startedAt,
                    endedAt,
                    playerAHeartsLeft,
                    playerBHeartsLeft
            );
            GameHistoryManager.addGame(history);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ensures history is saved only once, converting remaining lives to points.
     */
    private void persistHistoryIfNeeded(boolean success, int heartsLeft) {
        if (historySaved) {
            return;
        }
        saveGameHistory(heartsLeft, heartsLeft, success);
        historySaved = true;
    }

    private enum SaveStatus {
        IN_PROGRESS,
        COMPLETED
    }

    private static class GameSaveData {
        SaveStatus status;
        String difficulty;
        int boardSize;
        String playerAName;
        String playerBName;
        String playerAThemeId;
        String playerBThemeId;
        boolean isPlayerATurn;
        int lives;
        int score;
        long timerElapsedMillis;
        String startedAtIso;
        long lastUpdatedEpochMillis;
        SavedBoard boardA;
        SavedBoard boardB;
    }

    private static class SavedBoard {
        SavedCell[][] cells;
        String themeId;
        boolean[][] mineFlagRewarded;
    }

    private static class SavedCell {
        boolean revealed;
        boolean flagged;
        CellType type;
        int adjacentMines;
        boolean surpriseUsed;
        boolean questionUsed;
        Question question;
    }

    private static class SavedGameRepository {
        private static Path dir() throws IOException {
            Path dir = Paths.get(System.getProperty("user.home"), ".minesweeper-lion");
            Files.createDirectories(dir);
            return dir;
        }

        private static Path pathForKey(String key) throws IOException {
            return dir().resolve(key + ".json");
        }

        static void save(String key, GameSaveData data) {
            try (BufferedWriter writer = Files.newBufferedWriter(pathForKey(key))) {
                GSON.toJson(data, writer);
            } catch (IOException e) {
                System.err.println("Failed to save game state: " + e.getMessage());
            }
        }

        static GameSaveData loadLatest(String key) {
            try (BufferedReader reader = Files.newBufferedReader(pathForKey(key))) {
                return GSON.fromJson(reader, GameSaveData.class);
            } catch (IOException | JsonSyntaxException e) {
                delete(key);
                return null;
            }
        }

        static void delete(String key) {
            try {
                Files.deleteIfExists(pathForKey(key));
            } catch (IOException e) {
                System.err.println("Failed to clear saved game: " + e.getMessage());
            }
        }
    }
}
