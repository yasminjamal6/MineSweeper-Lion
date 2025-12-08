package main.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

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

    @FXML
    private void initialize() {
        // אנימציית Fade-In קלה לכל המסך
        if (root != null) {
            root.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setToValue(1.0);
            ft.play();
        }
    }

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
                gifLabel.setText("");
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
            // זה חלון התוצאה הקטן (dialog)
            Stage dialogStage = (Stage) root.getScene().getWindow();
            Window owner = dialogStage.getOwner();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();

            if (owner instanceof Stage mainStage) {
                Scene scene = mainStage.getScene();
                if (scene == null) {
                    scene = new Scene(newRoot);
                    mainStage.setScene(scene);
                } else {
                    scene.setRoot(newRoot);
                }

                // להתאים גודל חלון לגודל המסך החדש
                mainStage.sizeToScene();
                mainStage.setMinWidth(mainStage.getWidth());
                mainStage.setMinHeight(mainStage.getHeight());

                // לסגור את חלון התוצאה
                dialogStage.close();
            } else {
                // במקרה שאין owner (למשל כשמריצים רק את ה־FXML לבד)
                Scene scene = dialogStage.getScene();
                scene.setRoot(newRoot);
                dialogStage.sizeToScene();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
