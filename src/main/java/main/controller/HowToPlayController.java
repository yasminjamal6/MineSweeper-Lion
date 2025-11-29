package main.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class HowToPlayController {

    @FXML private HBox headerRow;
    @FXML private ScrollPane contentCard;
    @FXML private HBox actionsRow;

    @FXML
    private void initialize() {
        playEntranceAnimations();
    }

    private void playEntranceAnimations() {
        if (headerRow == null || contentCard == null || actionsRow == null) {
            return;
        }

        headerRow.setOpacity(0);
        contentCard.setOpacity(0);
        actionsRow.setOpacity(0);

        headerRow.setTranslateY(-30);
        contentCard.setTranslateY(20);
        actionsRow.setTranslateY(40);

        FadeTransition headerFade = new FadeTransition(Duration.millis(500), headerRow);
        headerFade.setFromValue(0);
        headerFade.setToValue(1);

        TranslateTransition headerMove = new TranslateTransition(Duration.millis(500), headerRow);
        headerMove.setFromY(-30);
        headerMove.setToY(0);
        headerMove.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition contentFade = new FadeTransition(Duration.millis(550), contentCard);
        contentFade.setFromValue(0);
        contentFade.setToValue(1);
        contentFade.setDelay(Duration.millis(150));

        TranslateTransition contentMove = new TranslateTransition(Duration.millis(550), contentCard);
        contentMove.setFromY(20);
        contentMove.setToY(0);
        contentMove.setInterpolator(Interpolator.EASE_OUT);
        contentMove.setDelay(Duration.millis(150));

        FadeTransition actionsFade = new FadeTransition(Duration.millis(600), actionsRow);
        actionsFade.setFromValue(0);
        actionsFade.setToValue(1);
        actionsFade.setDelay(Duration.millis(300));

        TranslateTransition actionsMove = new TranslateTransition(Duration.millis(600), actionsRow);
        actionsMove.setFromY(40);
        actionsMove.setToY(0);
        actionsMove.setInterpolator(Interpolator.EASE_OUT);
        actionsMove.setDelay(Duration.millis(300));

        ParallelTransition all = new ParallelTransition(
                headerFade, headerMove,
                contentFade, contentMove,
                actionsFade, actionsMove
        );

        all.play();
    }

    @FXML
    private void onCloseHowToPlay(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/home-view.fxml")
            );
            Parent newRoot = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();

            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(250), newRoot);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onStartGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/game-setup-view.fxml")
            );
            Parent newRoot = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();

            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(250), newRoot);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
