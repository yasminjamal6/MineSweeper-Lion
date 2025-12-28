package main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Question;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.Random;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Controller for the single-question popup.
 * <p>
 * Displays a trivia question with four possible answers, manages
 * a countdown timer, tracks whether the player answered in time,
 * and visually highlights correct/incorrect choices before closing.
 * </p>
 */
public class QuestionController {

    @FXML private Label questionLabel;
    @FXML private Label categoryLabel;
    @FXML private Label timerLabel;
    @FXML private Label levelLabel;
    @FXML private javafx.scene.layout.AnchorPane questionRoot;



    @FXML private Button ansBtn0, ansBtn1, ansBtn2, ansBtn3;

    private Button[] answerButtons;
    private Question question;
    private int secondsLeft;
    private Timeline timeline;

    private boolean answeredCorrect = false;
    private boolean answered = false;

    @FXML
    private void initialize() {
        answerButtons = new Button[]{ansBtn0, ansBtn1, ansBtn2, ansBtn3};
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setUserData(i);
        }
    }

    public void setQuestion(Question q) {
        this.question = q;

        secondsLeft = 30;
        answeredCorrect = false;
        answered = false;
        stopTimer();

        questionLabel.setText(q.getText());
        categoryLabel.setText("");
        categoryLabel.setVisible(false);

        if (levelLabel != null) {
            String levelText = "LEVEL: UNKNOWN";

            if (q.getLevel() != null) {
                levelText = switch (q.getLevel()) {
                    case EASY -> "LEVEL: EASY";
                    case MEDIUM -> "LEVEL: MEDIUM";
                    case HARD -> "LEVEL: HARD";
                    case EXPERT -> "LEVEL: EXPERT";
                    default -> "LEVEL: UNKNOWN";
                };
            }

            levelLabel.setText(levelText);
            levelLabel.setVisible(true);

            levelLabel.getStyleClass().removeAll("level-easy", "level-medium", "level-hard", "level-expert");

            if (q.getLevel() != null) {
                switch (q.getLevel()) {
                    case EASY -> levelLabel.getStyleClass().add("level-easy");
                    case MEDIUM -> levelLabel.getStyleClass().add("level-medium");
                    case HARD -> levelLabel.getStyleClass().add("level-hard");
                    case EXPERT -> levelLabel.getStyleClass().add("level-expert");
                    default -> { }
                }
            }
        }

        String[] opts = q.getOptions();
        for (int i = 0; i < 4; i++) {
            answerButtons[i].setText(opts[i]);
            answerButtons[i].setDisable(false);
            answerButtons[i].getStyleClass().removeAll("answer-correct", "answer-wrong");
        }

        startTimer();
    }

    /**
     * Starts the countdown timer and updates the timer label
     * every second until it expires or is stopped.
     */
    private void startTimer() {
        timerLabel.setText(String.valueOf(secondsLeft));

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsLeft--;
            timerLabel.setText(String.valueOf(secondsLeft));

            if (secondsLeft <= 0) {
                stopTimer();
                onTimeOut();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void playSound(String soundFile) {
        try {
            Media media = new Media(
                    getClass().getResource("/sound/" + soundFile).toExternalForm()
            );
            MediaPlayer player = new MediaPlayer(media);
            player.play();
        } catch (Exception e) {
            System.err.println("Could not play sound: " + soundFile);
        }
    }

    private void stopTimer() {
        if (timeline != null) timeline.stop();
    }
    @FXML
    private void onAnswerClick(ActionEvent event) {
        if (answered) return;
        answered = true;
        stopTimer();

        Button clicked = (Button) event.getSource();
        int chosenIndex = (int) clicked.getUserData();
        int correctIndex = question.getCorrectIndex();

        if (chosenIndex == correctIndex) {
            answeredCorrect = true;
            clicked.getStyleClass().add("answer-correct");
            playCorrectEffect(clicked);   // ✅
        } else {
            clicked.getStyleClass().add("answer-wrong");
            answerButtons[correctIndex].getStyleClass().add("answer-correct");
            playWrongEffect(clicked);     // ❌
        }

        for (Button b : answerButtons) b.setDisable(true);

        PauseTransition pt = new PauseTransition(Duration.seconds(1.2));
        pt.setOnFinished(e -> closeWindow());
        pt.play();
    }

    private void playCorrectEffect(Button clicked) {
        // Pop animation (happy)
        ScaleTransition pop = new ScaleTransition(Duration.millis(180), clicked);
        pop.setFromX(1.0);
        pop.setFromY(1.0);
        pop.setToX(1.08);
        pop.setToY(1.08);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        pop.play();

        // Confetti burst
        spawnConfetti(clicked, 18);

        playSound("WIN.wav");

    }

    private void playWrongEffect(Button clicked) {
        // Shake animation (sad)
        TranslateTransition shake = new TranslateTransition(Duration.millis(45), clicked);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);

        // Small shrink for extra feedback
        ScaleTransition shrink = new ScaleTransition(Duration.millis(120), clicked);
        shrink.setFromX(1.0);
        shrink.setFromY(1.0);
        shrink.setToX(0.97);
        shrink.setToY(0.97);
        shrink.setAutoReverse(true);
        shrink.setCycleCount(2);

        new ParallelTransition(shake, shrink).play();
        playSound("lose.wav");
    }


    private void onTimeOut() {
        int correctIndex = question.getCorrectIndex();
        answerButtons[correctIndex].getStyleClass().add("answer-correct");
        for (Button b : answerButtons) b.setDisable(true);

        PauseTransition pt = new PauseTransition(Duration.seconds(1.0));
        pt.setOnFinished(e -> closeWindow());
        pt.play();
    }

    private final Random rnd = new Random();

    private void spawnConfetti(Node source, int amount) {
        if (questionRoot == null) return;

        // Find the center of the clicked button in the AnchorPane coordinates
        Bounds b = source.localToScene(source.getBoundsInLocal());
        Bounds rootBounds = questionRoot.sceneToLocal(b);

        double cx = (rootBounds.getMinX() + rootBounds.getMaxX()) / 2.0;
        double cy = (rootBounds.getMinY() + rootBounds.getMaxY()) / 2.0;

        for (int i = 0; i < amount; i++) {
            Circle dot = new Circle(3 + rnd.nextDouble() * 3);

            // random bright-ish colors
            dot.setFill(Color.hsb(rnd.nextDouble() * 360, 0.85, 1.0));

            dot.setLayoutX(cx);
            dot.setLayoutY(cy);

            questionRoot.getChildren().add(dot);

            double dx = -120 + rnd.nextDouble() * 240;
            double dy = -140 + rnd.nextDouble() * 180;

            TranslateTransition fly = new TranslateTransition(Duration.millis(520 + rnd.nextInt(250)), dot);
            fly.setByX(dx);
            fly.setByY(dy);

            FadeTransition fade = new FadeTransition(Duration.millis(650), dot);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(fly, fade);
            pt.setOnFinished(e -> questionRoot.getChildren().remove(dot));
            pt.play();
        }
    }

    @FXML
    private void onClose() {
        stopTimer();
        closeWindow();
    }

    private void closeWindow() {
        stopTimer();
        Stage stage = (Stage) questionLabel.getScene().getWindow();
        stage.close();
    }

    public boolean isAnsweredCorrect() { return answeredCorrect; }
    public boolean wasAnswered() { return answered; }
}
