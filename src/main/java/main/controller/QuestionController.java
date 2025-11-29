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
        } else {
            clicked.getStyleClass().add("answer-wrong");
            answerButtons[correctIndex].getStyleClass().add("answer-correct");
        }

        for (Button b : answerButtons) b.setDisable(true);

        PauseTransition pt = new PauseTransition(Duration.seconds(1.2));
        pt.setOnFinished(e -> closeWindow());
        pt.play();
    }

    private void onTimeOut() {
        int correctIndex = question.getCorrectIndex();
        answerButtons[correctIndex].getStyleClass().add("answer-correct");
        for (Button b : answerButtons) b.setDisable(true);

        PauseTransition pt = new PauseTransition(Duration.seconds(1.0));
        pt.setOnFinished(e -> closeWindow());
        pt.play();
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
