package main.controller;

import com.google.gson.Gson;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.GameHistory;
import model.GameHistoryManager;
import model.Difficulty;
import model.Theme;
import model.ThemeColors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.util.Duration;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

/**
 * Controller for the Game History view. Manages loading, displaying, filtering,
 * and summarizing game history records from the GameHistoryManager.
 */
public class GameHistoryController {

    @FXML private TableView<GameHistory> historyTable;
    @FXML private TableColumn<GameHistory, String> startedAtCol;
    @FXML private TableColumn<GameHistory, String> playerACol;
    @FXML private TableColumn<GameHistory, String> playerBCol;
    @FXML private TableColumn<GameHistory, String> difficultyCol;
    @FXML private TableColumn<GameHistory, Number> scoreCol;
    @FXML private TableColumn<GameHistory, Number> livesCol;
    @FXML private TableColumn<GameHistory, String> resultCol;
    @FXML private TableColumn<GameHistory, Number> durationCol;  //not yet implemented in this iteration
    @FXML private TextField searchField;
    @FXML private HBox resumeCard;
    @FXML private Button resumeBtn;

    private ObservableList<GameHistory> masterData;
    private ResumeInfo availableResume;


    @FXML private Label totalGamesLabel;
    @FXML private Label easyGamesLabel;
    @FXML private Label mediumGamesLabel;
    @FXML private Label hardGamesLabel;

    // elements for animation
    @FXML private HBox headerRow;
    @FXML private HBox statsRow;
    @FXML private VBox tableCard;



    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");



    /**
     * Initializes the controller: loads game history, calculates stats,
     * sets up table columns, implements search functionality, and plays entrance animations.
     */
    @FXML
    private void initialize() {
        List<GameHistory> history = GameHistoryManager.getHistory();

        // ---- Stats ----
        int total = history.size();
        long easy = history.stream()
                .filter(g -> g.getDifficulty() == Difficulty.EASY)
                .count();
        long medium = history.stream()
                .filter(g -> g.getDifficulty() == Difficulty.MEDIUM)
                .count();
        long hard = history.stream()
                .filter(g -> g.getDifficulty() == Difficulty.HARD)
                .count();

        totalGamesLabel.setText(String.valueOf(total));
        easyGamesLabel.setText(String.valueOf(easy));
        mediumGamesLabel.setText(String.valueOf(medium));
        hardGamesLabel.setText(String.valueOf(hard));

        // ---- Table columns ----

        // Date column
        startedAtCol.setCellValueFactory(cellData -> {
            GameHistory g = cellData.getValue();
            if (g.getStartedAt() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(g.getStartedAt().format(formatter));
        });

        // Player A / Player B
        playerACol.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getPlayerAName())
        );

        playerBCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getPlayerBName())
        );

        // Difficulty
        difficultyCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDifficultyString())
        );

        // Shared score
        scoreCol.setCellValueFactory(
                cellData -> new SimpleLongProperty(cellData.getValue().getScore())
        );

        // Shared lives
        livesCol.setCellValueFactory(
                cellData -> new SimpleLongProperty(cellData.getValue().getSharedLives())
        );

        // Result: Win / Loss based on shared lives
        resultCol.setCellValueFactory(cellData -> {
            GameHistory g = cellData.getValue();
            if (g.getSharedLives() > 0) {
                return new SimpleStringProperty("Win");
            } else {
                return new SimpleStringProperty("Loss");
            }
        });

        // Custom cell factory for styled result display (emoji + color)
        resultCol.setCellFactory(column -> new TableCell<GameHistory, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                if (item.equals("Win")) {
                    setText("Win 🏆");
                    setStyle("-fx-text-fill: #facc15; -fx-font-weight: bold;");
                } else {
                    setText("Loss ✖");
                    setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });

        // ---- Table data with search functionality ----
        masterData = FXCollections.observableArrayList(history);

        // 1) FilteredList – starts with no filtering
        FilteredList<GameHistory> filteredData =
                new FilteredList<>(masterData, g -> true);

        // 2) Listen to search field changes
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> {
                String filter = (newValue == null) ? "" : newValue.toLowerCase().trim();
                // Predicate defines the criteria for which elements are visible
                filteredData.setPredicate(g -> {
                    if (filter.isEmpty()) {
                        return true; // Show all
                    }

                    // Prepare all values as strings
                    String a = g.getPlayerAName() == null ? "" : g.getPlayerAName().toLowerCase();
                    String b = g.getPlayerBName() == null ? "" : g.getPlayerBName().toLowerCase();
                    String diff = g.getDifficultyString() == null ? "" : g.getDifficultyString().toLowerCase();
                    String result = g.getResult().toLowerCase();
                    String scoreStr = String.valueOf(g.getScore());
                    String livesStr = String.valueOf(g.getSharedLives());

                    return a.contains(filter)
                            || b.contains(filter)
                            || diff.contains(filter)
                            || result.contains(filter)
                            || scoreStr.contains(filter)
                            || livesStr.contains(filter);
                });
            });
        }

        // 3) SortedList: Wraps filtered data and enables column sorting
        SortedList<GameHistory> sortedData = new SortedList<>(filteredData);
        // Bind the table's sorting property to the sorted list
        sortedData.comparatorProperty().bind(historyTable.comparatorProperty());

        historyTable.setItems(sortedData);

        // ---- Entrance animations ----
        playEntranceAnimations();

        availableResume = findResume();
        boolean hasSaved = availableResume != null;
        if (resumeCard != null) {
            resumeCard.setVisible(hasSaved);
            resumeCard.setManaged(hasSaved);
        }
        if (resumeBtn != null) {
            resumeBtn.setDisable(!hasSaved);
        }
    }

        // --- UI/Animation Logic ---

    /**
     * Plays the coordinated entrance animations (Fade and Slide) for the main UI components.
     */
    private void playEntranceAnimations() {
        if (headerRow == null || statsRow == null || tableCard == null) return;

       //Set initial state (invisible and offset)        headerRow.setOpacity(0);
        statsRow.setOpacity(0);
        tableCard.setOpacity(0);

        headerRow.setTranslateY(-30);
        statsRow.setTranslateY(-20);
        tableCard.setTranslateY(40);

        // header animation
        FadeTransition headerFade = new FadeTransition(Duration.millis(500), headerRow);
        headerFade.setFromValue(0);
        headerFade.setToValue(1);

        TranslateTransition headerMove = new TranslateTransition(Duration.millis(500), headerRow);
        headerMove.setFromY(-30);
        headerMove.setToY(0);
        headerMove.setInterpolator(Interpolator.EASE_OUT);

        // stats row animation
        FadeTransition statsFade = new FadeTransition(Duration.millis(550), statsRow);
        statsFade.setFromValue(0);
        statsFade.setToValue(1);
        statsFade.setDelay(Duration.millis(150));

        TranslateTransition statsMove = new TranslateTransition(Duration.millis(550), statsRow);
        statsMove.setFromY(-20);
        statsMove.setToY(0);
        statsMove.setInterpolator(Interpolator.EASE_OUT);
        statsMove.setDelay(Duration.millis(150));

        // table card animation
        FadeTransition tableFade = new FadeTransition(Duration.millis(600), tableCard);
        tableFade.setFromValue(0);
        tableFade.setToValue(1);
        tableFade.setDelay(Duration.millis(300));

        TranslateTransition tableMove = new TranslateTransition(Duration.millis(600), tableCard);
        tableMove.setFromY(40);
        tableMove.setToY(0);
        tableMove.setInterpolator(Interpolator.EASE_OUT);
        tableMove.setDelay(Duration.millis(300));

        // Group all transitions to play simultaneously
        ParallelTransition all = new ParallelTransition(
                headerFade, headerMove,
                statsFade, statsMove,
                tableFade, tableMove
        );

        all.play();
    }


    // --- Navigation Handlers ---

    /**
     * Loads the Home View, navigating away from the history screen.
     */
    @FXML
    private void onBackToHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/view/home-view.fxml")
        );
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
    }

    /**
     * Loads the Game Setup View to start a new game.
     */
    @FXML
    private void onNewGame(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/view/game-setup-view.fxml")
        );
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
    }

    @FXML
    private void onResumeSavedGame(ActionEvent event) throws IOException {
        if (availableResume == null) {
            return;
        }

        GameSetupController.selectedPlayerAName = availableResume.playerAName;
        GameSetupController.selectedPlayerBName = availableResume.playerBName;
        GameSetupController.selectedDifficulty = availableResume.difficultyEnum;
        GameSetupController.selectedThemeA = findThemeById(availableResume.playerAThemeId, GameSetupController.selectedThemeA);
        GameSetupController.selectedThemeB = findThemeById(availableResume.playerBThemeId, GameSetupController.selectedThemeB);

        Parent root = FXMLLoader.load(
                getClass().getResource("/view/game.fxml")
        );
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
    }

    private ResumeInfo findResume() {
        try {
            Path dir = Paths.get(System.getProperty("user.home"), ".minesweeper-lion");
            if (!Files.exists(dir)) {
                return null;
            }
            try (var stream = Files.list(dir)) {
                Optional<Path> first = stream
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .findFirst();
                if (first.isEmpty()) {
                    return null;
                }
                return readResumeInfo(first.get());
            }
        } catch (Exception e) {
            return null;
        }
    }

    private ResumeInfo readResumeInfo(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            Gson gson = new Gson();
            ResumeInfo info = gson.fromJson(reader, ResumeInfo.class);
            if (info == null || info.status == null || !"IN_PROGRESS".equalsIgnoreCase(info.status)) {
                return null;
            }
            info.difficultyEnum = info.parseDifficulty();
            return info.difficultyEnum != null ? info : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Theme findThemeById(String id, Theme fallback) {
        if (id != null) {
            for (Theme t : ThemeColors.themes) {
                if (id.equals(t.id)) {
                    return t;
                }
            }
        }
        return fallback;
    }

    private static class ResumeInfo {
        String status;
        String playerAName;
        String playerBName;
        String difficulty;
        String playerAThemeId;
        String playerBThemeId;

        transient GameSetupController.Difficulty difficultyEnum;

        GameSetupController.Difficulty parseDifficulty() {
            try {
                return GameSetupController.Difficulty.valueOf(difficulty);
            } catch (Exception e) {
                return null;
            }
        }
    }

}
