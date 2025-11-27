package main.controller;

import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HomeController {

    // ----- BACKGROUND VIDEO -----
    @FXML
    private MediaView videoView;

    private MediaPlayer backgroundPlayer;

    @FXML
    public void initialize() {
        try {
            // שימי לב: הנתיב חייב להתאים בדיוק למיקום בקבצים
            URL videoUrl = getClass().getResource("/images/lion_bg.mp4");
            if (videoUrl == null) {
                System.out.println("ERROR: Video file NOT FOUND at /images/lion_bg.mp4");
                return;
            }

            Media media = new Media(videoUrl.toExternalForm());
            backgroundPlayer = new MediaPlayer(media);

            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // לופ אינסופי
            backgroundPlayer.setMute(true);                         // בלי סאונד
            videoView.setMediaPlayer(backgroundPlayer);
            backgroundPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----- ניווטים מהמסך הראשי -----

    @FXML
    private void onStartAdventure(ActionEvent event) {
        System.out.println(">> onStartAdventure clicked");
        switchSceneWithFade(event, "/view/game-setup-view.fxml");
    }

    @FXML
    private void onHowToPlay(ActionEvent event) {
        System.out.println(">> How to Play clicked");
        switchSceneWithFade(event, "/view/how-to-play.fxml");
    }

    private void switchSceneWithFade(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newRoot = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();

            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(250), newRoot);
            ft.setToValue(1);
            ft.play();

            // כשעוברים מסך – לעצור את הווידאו של הבית
            if (backgroundPlayer != null) {
                backgroundPlayer.stop();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/settings-view.fxml")
            );
            Parent popup = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(popup));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onHistory() {
        System.out.println("History clicked");
    }

    @FXML
    private void onManageQuestions() {
        System.out.println("Question Manager clicked");
    }
}
