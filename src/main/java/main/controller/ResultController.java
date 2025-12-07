package main.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ResultController {

    @FXML private StackPane root;

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    @FXML private Label scoreValueLabel;
    @FXML private Label scoreLabel;

    @FXML private Label playerALabel;
    @FXML private Label playerBLabel;

    @FXML private Label heartsLabel;
    @FXML private Label quoteLabel;

    private int baseScore;
    private int hearts;
    private int heartValue;

    // ========================
    //     WIN SCREEN SETUP
    // ========================
    public void initAsWin(String playerA, String playerB,
                          int baseScore, int hearts, int heartValue) {

        this.baseScore = baseScore;
        this.hearts = hearts;
        this.heartValue = heartValue;

        int bonus = hearts * heartValue;
        int total = baseScore + bonus;

        titleLabel.setText("!המלך ניצח 👑");
        subtitleLabel.setText("כל המוקשים נוטרלו ✔");

        playerALabel.setText(playerA);
        playerBLabel.setText(playerB);

        scoreValueLabel.setText(String.valueOf(total));
        scoreLabel.setText("(+" + bonus + " from hearts)");

        heartsLabel.setText("לבבות: " + hearts);
        quoteLabel.setText("Only kings survive the jungle");
    }

    // ========================
    //     LOSE SCREEN SETUP
    // ========================
    public void initAsLose(String playerA, String playerB,
                           int baseScore, int hearts, int heartValue) {

        this.baseScore = baseScore;
        this.hearts = hearts;
        this.heartValue = heartValue;

        int bonus = hearts * heartValue;
        int total = baseScore + bonus;

        titleLabel.setText("הצבועים השתלטו! 😈");
        subtitleLabel.setText("נגמרו הלבבות... 💔");

        playerALabel.setText(playerA);
        playerBLabel.setText(playerB);

        scoreValueLabel.setText(String.valueOf(total));
        scoreLabel.setText("(+" + bonus + " from hearts)");

        heartsLabel.setText("לבבות: " + hearts);
        quoteLabel.setText("Even kings can fall");
    }

    // ========================
    //       BUTTONS
    // ========================

    @FXML
    private void onPlayAgain(ActionEvent event) {
        switchMainStageScene("/view/game-setup-view.fxml");
    }

    @FXML
    private void onHome(ActionEvent event) {
        switchMainStageScene("/view/home-view.fxml");
    }

    // ============================
    //  SWITCH MAIN WINDOW SCENE
    // ============================
    private void switchMainStageScene(String fxmlPath) {

        try {
            // חלון התוצאה (הקטן)
            Stage dialogStage = (Stage) root.getScene().getWindow();
            Window owner = dialogStage.getOwner();

            if (owner instanceof Stage mainStage) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent newRoot = loader.load();

                Scene scene = new Scene(newRoot);
                mainStage.setScene(scene);
                mainStage.centerOnScreen();
            }

            dialogStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
