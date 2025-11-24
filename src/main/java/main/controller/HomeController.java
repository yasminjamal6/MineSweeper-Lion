package main.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.animation.FadeTransition;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.util.Duration;

public class HomeController {

    @FXML
    private void onStartAdventure(ActionEvent event) {
        System.out.println(">> onStartAdventure clicked");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/game-setup-view.fxml")
            );
            Parent newRoot = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();

            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(250), newRoot);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onHowToPlay(ActionEvent event) {
        System.out.println(">> How to Play clicked");

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/how-to-play.fxml")
            );
            Parent newRoot = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();

            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(250), newRoot);
            ft.setToValue(1);
            ft.play();

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
            stage.initModality(Modality.APPLICATION_MODAL); // חוסם את המסך הראשי עד שסוגרים
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