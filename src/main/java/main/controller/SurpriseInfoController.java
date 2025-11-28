package main.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Difficulty;

public class SurpriseInfoController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label costLabel;
    @FXML private Label badDetailsLabel;
    @FXML private Label goodDetailsLabel;
    @FXML private Button openButton;

    private Difficulty difficulty;

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;

        int cost        = difficulty.getActivationCostPoints();
        int goodPoints  = difficulty.getSurpriseGoodPoints();
        int badPoints   = difficulty.getSurpriseBadPoints();
        int mines       = difficulty.getMines();
        int question    = difficulty.getQuestionCells();
        int surprises   = difficulty.getSurpriseCells();
        int initialLives= difficulty.getInitialLives();

        switch (difficulty) {
            case EASY -> titleLabel.setText("Cub Training – Savannah Surprise Tile!");
            case MEDIUM -> titleLabel.setText("Young Lion Challenge – Savannah Surprise Tile!");
            case HARD -> titleLabel.setText("King of the Savannah – Savannah Surprise Tile!");
        }

        String subtitle = String.format(
                "Each board hides %d hyenas (mines), %d question tiles and %d surprise tiles. " +
                        "You start this journey with %d Lion Hearts.",
                mines, question, surprises, initialLives
        );
        subtitleLabel.setText(subtitle);

        costLabel.setText("Activation cost: " + cost + " points");

        badDetailsLabel.setText(
                String.format("-1 Lion Heart and -%d points", Math.abs(badPoints))
        );

        goodDetailsLabel.setText(
                String.format("+1 Lion Heart and +%d points", goodPoints)
        );
    }

    @FXML
    private void onOpenSurprise() {
        Stage stage = (Stage) openButton.getScene().getWindow();
        stage.close();
    }
}
