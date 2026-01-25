package main.controller;

import javafx.geometry.Insets;
import java.util.Optional;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.util.ResourceUtils;
import javafx.scene.control.*;
import model.PlayerProfileManager;
import model.Session;

/**
 * Controller for the Home screen.
 * <p>
 * Applies the current theme and language, plays entrance animations,
 * and provides navigation to the main game flow, how-to-play page,
 * settings dialog, game history, and question manager.
 * </p>
 */
public class HomeController {
    private static final String ADMIN_PASSWORD = "lionking"; // תשני למה שאת רוצה
    @FXML private VBox heroSection;
    @FXML private VBox buttonsSection;
    @FXML private HBox featuresRow;
    @FXML private VBox footerBox;
    @FXML private Pane tickerClip;
    @FXML private Label tickerLabel;
    @FXML private StackPane rootPane;



    @FXML
    private void initialize() {
        // DARK / LIGHT + Brightness
        SettingsController.applyThemeToRoot(rootPane);
        SettingsController.refreshLanguageOnAllWindows();
        playEntranceAnimations();
        javafx.application.Platform.runLater(this::startTicker);

    }


    private void startTicker() {
        String tickerText =
                "🦁 Every move shapes your destiny.   •   " +
                        "✨ Tip: Use logic, not luck.   •   " +
                        "🔥 Pride Rock trivia awaits!   •   " +
                        "🐾 Choose wisely before the sun sets.   •   " +
                        "🏆 Build your legend in the savanna!   •   ";

        // כדי שיהיה “רציף”, נשכפל פעמיים
        tickerLabel.setText(tickerText + tickerText);

        double clipW = tickerClip.getWidth();
        double textW = tickerLabel.prefWidth(-1);

        tickerLabel.setTranslateX(clipW);

        javafx.animation.TranslateTransition tt =
                new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(18), tickerLabel);

        tt.setFromX(clipW);
        tt.setToX(-textW);
        tt.setInterpolator(javafx.animation.Interpolator.LINEAR);

        // לופ אינסופי
        tt.setCycleCount(javafx.animation.Animation.INDEFINITE);
        tt.play();
    }

    /**
     * Plays entrance animations for the hero section, main buttons,
     * feature row, and footer to create a smooth landing effect.
     */
    private void playEntranceAnimations() {
        if (heroSection == null || buttonsSection == null
                || featuresRow == null || footerBox == null) {
            return;
        }

        heroSection.setOpacity(0);
        buttonsSection.setOpacity(0);
        featuresRow.setOpacity(0);
        footerBox.setOpacity(0);

        heroSection.setTranslateY(-40);
        buttonsSection.setTranslateY(30);
        featuresRow.setTranslateY(40);
        footerBox.setTranslateY(50);

        FadeTransition heroFade = new FadeTransition(Duration.millis(550), heroSection);
        heroFade.setFromValue(0);
        heroFade.setToValue(1);

        TranslateTransition heroMove = new TranslateTransition(Duration.millis(550), heroSection);
        heroMove.setFromY(-40);
        heroMove.setToY(0);
        heroMove.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition buttonsFade = new FadeTransition(Duration.millis(580), buttonsSection);
        buttonsFade.setFromValue(0);
        buttonsFade.setToValue(1);
        buttonsFade.setDelay(Duration.millis(150));

        TranslateTransition buttonsMove = new TranslateTransition(Duration.millis(580), buttonsSection);
        buttonsMove.setFromY(30);
        buttonsMove.setToY(0);
        buttonsMove.setInterpolator(Interpolator.EASE_OUT);
        buttonsMove.setDelay(Duration.millis(150));

        FadeTransition featuresFade = new FadeTransition(Duration.millis(610), featuresRow);
        featuresFade.setFromValue(0);
        featuresFade.setToValue(1);
        featuresFade.setDelay(Duration.millis(280));

        TranslateTransition featuresMove = new TranslateTransition(Duration.millis(610), featuresRow);
        featuresMove.setFromY(40);
        featuresMove.setToY(0);
        featuresMove.setInterpolator(Interpolator.EASE_OUT);
        featuresMove.setDelay(Duration.millis(280));

        FadeTransition footerFade = new FadeTransition(Duration.millis(640), footerBox);
        footerFade.setFromValue(0);
        footerFade.setToValue(1);
        footerFade.setDelay(Duration.millis(380));

        TranslateTransition footerMove = new TranslateTransition(Duration.millis(640), footerBox);
        footerMove.setFromY(50);
        footerMove.setToY(0);
        footerMove.setInterpolator(Interpolator.EASE_OUT);
        footerMove.setDelay(Duration.millis(380));

        ParallelTransition all = new ParallelTransition(
                heroFade, heroMove,
                buttonsFade, buttonsMove,
                featuresFade, featuresMove,
                footerFade, footerMove
        );

        all.play();
    }

    @FXML
    private void onStartAdventure(ActionEvent event) {
        System.out.println(">> onStartAdventure clicked");
        try {
            var url = ResourceUtils.url(getClass(), "/view/game-setup-view.fxml");
            if (url == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent newRoot = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();

            newRoot.setOpacity(0);
            scene.setRoot(newRoot);
            SettingsController.applyThemeToRoot(newRoot);

            FadeTransition ft = new FadeTransition(Duration.millis(250), newRoot);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onHowToPlay(ActionEvent event) {
        try {
            var url = ResourceUtils.url(getClass(), "/view/how-to-play.fxml");
            if (url == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent newRoot = loader.load();

            SettingsController.applyThemeToRoot(newRoot);
            SettingsController.refreshLanguageOnAllWindows();

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
    private void openSettings(ActionEvent event) {
        try {
            var url = ResourceUtils.url(getClass(), "/view/settings-view.fxml");
            if (url == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent popup = loader.load();

            SettingsController.applyThemeToRoot(popup);
            SettingsController.refreshLanguageOnAllWindows();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.setScene(new Scene(popup));

            Stage mainStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            settingsStage.initOwner(mainStage);

            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.setResizable(false);
            settingsStage.centerOnScreen();

            settingsStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onOpenShop(ActionEvent event) {
        try {
            var url = ResourceUtils.url(getClass(), "/view/shopView.fxml");
            if (url == null) return;

            FXMLLoader loader = new FXMLLoader(url);
            Parent newRoot = loader.load();

            SettingsController.applyThemeToRoot(newRoot);
            SettingsController.refreshLanguageOnAllWindows();

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
    private void onProfile(ActionEvent event) {
        String viewerName = requestPlayerAccess();
        if (viewerName == null || viewerName.isBlank()) {
            return;
        }

        try {
            Session.setActivePlayerName(viewerName);
            var url = ResourceUtils.url(getClass(), "/view/profile-view.fxml");
            if (url == null) return;

            FXMLLoader loader = new FXMLLoader(url);
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
    private void onHistory(ActionEvent event) {

        // 1) מבקשים סיסמה (אותו דיאלוג בדיוק)
        AccessResult access = requestAdminAccess();

        if (access == AccessResult.DENIED) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Access denied");
            a.setHeaderText("The pride remains protected.");
            a.setContentText("Incorrect password :( ");
            DialogPane dp = a.getDialogPane();
            String alertCss = ResourceUtils.externalForm(getClass(), "/css/alert.css");
            if (alertCss != null) {
                dp.getStylesheets().add(alertCss);
            }
            dp.getStyleClass().add("lion-alert");

            a.showAndWait();
            return;

        } else if (access == AccessResult.CANCELED) {
            return;
        }

        try {
            var url = ResourceUtils.url(getClass(), "/view/game-history.fxml");
            if (url == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            SettingsController.applyThemeToRoot(root);
            SettingsController.refreshLanguageOnAllWindows();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onQuestionManager(ActionEvent e) {

        // 1) מבקשים סיסמה
        AccessResult access = requestAdminAccess();
        if (access == AccessResult.DENIED) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Access denied");
            a.setHeaderText("The pride remains protected.");
            a.setContentText("Incorrect password :( ");
            DialogPane dp = a.getDialogPane();
            String alertCss = ResourceUtils.externalForm(getClass(), "/css/alert.css");
            if (alertCss != null) {
                dp.getStylesheets().add(alertCss);
            }
            dp.getStyleClass().add("lion-alert");

            a.showAndWait();
            return;
        } else if (access == AccessResult.CANCELED) {
            return; // user bailed out; do nothing
        }

        // 2) אם נכון -> נכנסים למסך
        try {
            var url = ResourceUtils.url(getClass(), "/view/question-manager-view.fxml");
            if (url == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            SettingsController.applyThemeToRoot(root);
            SettingsController.refreshLanguageOnAllWindows();

            Scene scene = ((Node)e.getSource()).getScene();
            scene.setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onProfiles(ActionEvent event) {
        try {
            var url = ResourceUtils.url(getClass(), "/view/player-profile.fxml");
            if (url == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            SettingsController.applyThemeToRoot(root);
            SettingsController.refreshLanguageOnAllWindows();

            Scene scene = ((Node) event.getSource()).getScene();
            root.setOpacity(0);
            scene.setRoot(root);

            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private AccessResult requestAdminAccess() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Pride Rock – Admin Access");
        dialog.setHeaderText("Only guardians of the pride may enter.");

        DialogPane dp = dialog.getDialogPane();

        // משתמשים באותו CSS כמו ה-Confirmation שיש לך
        String alertCss = ResourceUtils.externalForm(getClass(), "/css/alert.css");
        if (alertCss != null) {
            dp.getStylesheets().add(alertCss);
        }
        dp.getStyleClass().add("admin-dialog"); // נוסיף את העיצוב בצעד 4

        ButtonType enterBtn  = new ButtonType("Enter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dp.getButtonTypes().setAll(cancelBtn, enterBtn);

        Label msg = new Label("Enter the admin password to manage questions:");
        PasswordField pf = new PasswordField();
        pf.setPromptText("Admin password");

        VBox box = new VBox(10, msg, pf);
        box.setPadding(new Insets(10, 10, 10, 10));
        dp.setContent(box);

        // מחזיר את הטקסט רק אם לחצו Enter
        dialog.setResultConverter(bt -> bt == enterBtn ? pf.getText() : null);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() == null) {
            return AccessResult.CANCELED; // X / Cancel
        }

        return result.get().equals(ADMIN_PASSWORD)
                ? AccessResult.GRANTED
                : AccessResult.DENIED;
    }

    private String requestPlayerAccess() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Pride Rock — Player Profile Access");
        dialog.setHeaderText(null);

        DialogPane dp = dialog.getDialogPane();
        String alertCss = ResourceUtils.externalForm(getClass(), "/css/alert.css");
        if (alertCss != null) {
            dp.getStylesheets().add(alertCss);
        }
        dp.getStyleClass().add("admin-dialog");

        ButtonType enterBtn  = new ButtonType("Enter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dp.getButtonTypes().setAll(cancelBtn, enterBtn);

        Label msg = new Label("Enter your username to view your profile:");
        TextField tf = new TextField();
        tf.setPromptText("Username");

        Label error = new Label("Username not found");
        error.getStyleClass().add("access-error");
        error.setVisible(false);
        error.setManaged(false);

        VBox box = new VBox(10, msg, tf, error);
        box.setPadding(new Insets(10, 10, 10, 10));
        dp.setContent(box);

        Button enterButtonNode = (Button) dp.lookupButton(enterBtn);
        enterButtonNode.addEventFilter(ActionEvent.ACTION, e -> {
            String actual = tf.getText();
            String existing = findExistingProfileName(actual);
            if (existing != null) {
                error.setVisible(false);
                error.setManaged(false);
                tf.setText(existing);
                return;
            }
            error.setVisible(true);
            error.setManaged(true);
            e.consume();
        });

        dialog.setResultConverter(bt -> bt == enterBtn ? tf.getText() : null);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() == null || result.get().isBlank()) {
            return null;
        }
        return result.get().trim();
    }

    private enum AccessResult {
        GRANTED, DENIED, CANCELED
    }

    private String findExistingProfileName(String name) {
        if (name == null || name.isBlank()) return null;
        String target = name.trim();
        for (var profile : PlayerProfileManager.getProfiles()) {
            if (profile == null || profile.getPlayerName() == null) continue;
            if (profile.getPlayerName().equalsIgnoreCase(target)) {
                return profile.getPlayerName();
            }
        }
        return null;
    }

}
