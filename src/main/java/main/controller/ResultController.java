package main.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.media.AudioClip;


import java.io.InputStream;

public class ResultController {

    @FXML private StackPane root;

    @FXML private Label gifLabel;
    @FXML private Label emojiLabel;

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

    private void playSound(String resourcePath) {
        try {
            var url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("Sound not found: " + resourcePath);
                return;
            }

            System.out.println("Playing sound: " + url);

            javax.sound.sampled.AudioInputStream ais =
                    javax.sound.sampled.AudioSystem.getAudioInputStream(url);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(ais);
            clip.start();

        } catch (Exception e) {
            System.err.println("Error playing sound: " + resourcePath);
            e.printStackTrace();
        }
    }

    public void initAsWin(String playerA, String playerB,
                          int baseScore, int hearts, int heartValue) {

        this.baseScore = baseScore;
        this.hearts = hearts;
        this.heartValue = heartValue;

        int bonus = hearts * heartValue;
        int total = baseScore + bonus;

        emojiLabel.setText("👑");
        titleLabel.setText("The King has won!");
        subtitleLabel.setText("All mines have been cleared.");
        // משפט חדש, יותר חיובי
        quoteLabel.setText("The savannah belongs to you.");

        playerALabel.setText(playerA);
        playerBLabel.setText(playerB);

        scoreValueLabel.setText(String.valueOf(total));
        scoreLabel.setText("(+" + bonus + " from hearts)");

        heartsLabel.setText("Hearts: " + hearts);

        loadGif("/images/lion_win.gif");
        playSound("/sound/win.mp3");

    }

    public void initAsLose(String playerA, String playerB,
                           int baseScore, int hearts, int heartValue) {

        this.baseScore = baseScore;
        this.hearts = hearts;
        this.heartValue = heartValue;

        int bonus = hearts * heartValue;
        int total = baseScore + bonus;

        emojiLabel.setText("😈");
        titleLabel.setText("Hyenas took over!");
        subtitleLabel.setText("You ran out of hearts...");
        // משפט חדש, פחות דכאוני 🙂
        quoteLabel.setText("Every king gets another chance.");

        playerALabel.setText(playerA);
        playerBLabel.setText(playerB);

        scoreValueLabel.setText(String.valueOf(total));
        scoreLabel.setText("(+" + bonus + " from hearts)");

        heartsLabel.setText("Hearts: " + hearts);

        loadGif("/images/lion_lose.gif");
        playSound("/sound/lose.wav");

    }

    // ========================
    //         GIF
    // ========================
    private void loadGif(String path) {
        if (gifLabel == null) return;

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                Image image = new Image(is);
                ImageView iv = new ImageView(image);
                iv.setFitWidth(220);
                iv.setPreserveRatio(true);
                gifLabel.setGraphic(iv);
                gifLabel.setText("");
            } else {
                gifLabel.setText(""); // no image – empty
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========================
    //        BUTTONS
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
            // this is the small dialog window
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
