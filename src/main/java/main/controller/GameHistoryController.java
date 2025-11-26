package main.controller;

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
import javafx.stage.Stage;
import model.GameHistory;
import model.GameHistoryManager;
import model.Difficulty;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.util.Duration;

public class GameHistoryController {

    @FXML private TableView<GameHistory> historyTable;

    @FXML private TableColumn<GameHistory, String> startedAtCol;
    @FXML private TableColumn<GameHistory, String> playerACol;
    @FXML private TableColumn<GameHistory, String> playerBCol;
    @FXML private TableColumn<GameHistory, String> difficultyCol;
    @FXML private TableColumn<GameHistory, Number> scoreCol;
    @FXML private TableColumn<GameHistory, Number> livesCol;
    @FXML private TableColumn<GameHistory, String> resultCol;
    @FXML private TableColumn<GameHistory, Number> durationCol;

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

        // Date
        startedAtCol.setCellValueFactory(cellData -> {
            GameHistory g = cellData.getValue();
            if (g.getStartedAt() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(g.getStartedAt().format(formatter));
        });

        // Player A / B
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

        // Result: Win / Loss (based on shared lives)
        resultCol.setCellValueFactory(cellData -> {
            GameHistory g = cellData.getValue();
            if (g.getSharedLives() > 0) {
                return new SimpleStringProperty("Win");
            } else {
                return new SimpleStringProperty("Loss");
            }
        });

        // Colored Result column with emojis
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

        // ---- Table data ----
        ObservableList<GameHistory> data =
                FXCollections.observableArrayList(history);
        historyTable.setItems(data);

        // ---- Play entrance animations ----
        playEntranceAnimations();
    }

    private void playEntranceAnimations() {
        if (headerRow == null || statsRow == null || tableCard == null) return;

        // start invisible & slightly moved
        headerRow.setOpacity(0);
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

        ParallelTransition all = new ParallelTransition(
                headerFade, headerMove,
                statsFade, statsMove,
                tableFade, tableMove
        );

        all.play();
    }

    @FXML
    private void onBackToHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/view/home-view.fxml")
        );
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
    }

    @FXML
    private void onNewGame(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/view/game-setup-view.fxml")
        );
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
    }

}
