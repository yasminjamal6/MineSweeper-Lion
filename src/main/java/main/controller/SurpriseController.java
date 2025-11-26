package main.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.ScoreRules;

public class SurpriseController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Label pointsLabel;
    @FXML private Label livesLabel;
    @FXML private Button okButton;

    private ScoreRules.ScoreChange change;

    public void setData(ScoreRules.ScoreChange change) {
        this.change = change;

        int points = change.getPointsDelta();
        int lives = change.getLivesDelta();

        String pointsText = "Points: " + (points >= 0 ? "+" + points : points);
        String livesText = "Lives: "  + (lives  >= 0 ? "+" + lives  : lives);

        pointsLabel.setText(pointsText);
        livesLabel.setText(livesText);

        if (points > 0 || lives > 0) {
            titleLabel.setText("Lucky Surprise! 🎁");
            messageLabel.setText("You got a positive surprise.");
        } else if (points < 0 || lives < 0) {
            titleLabel.setText("Oops... 😈");
            messageLabel.setText("This surprise was a bit risky.");
        } else {
            titleLabel.setText("Neutral Surprise");
            messageLabel.setText("No big change this time.");
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }
}
