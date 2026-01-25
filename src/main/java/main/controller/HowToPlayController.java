package main.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import main.util.ResourceUtils;
/**
 * Controller for the "How to Play" screen.
 * <p>
 * Applies the current theme and language, plays entrance animations,
 * and handles navigation either back to the home screen or to the
 * game setup screen.
 * </p>
 */
public class HowToPlayController {

    @FXML private AnchorPane rootPane;
    @FXML private HBox headerRow;
    @FXML private ScrollPane contentCard;
    @FXML private HBox actionsRow;
    @FXML private Label titleLabel;
    @FXML private Button closeButton;
    @FXML private Button startButton;
    @FXML private Label objectiveTitleLabel;
    @FXML private Label objectiveTextLabel;
    @FXML private Label controlsTitleLabel;
    @FXML private Label controlsTextLabel;
    @FXML private Label numbersTitleLabel;
    @FXML private Label numbersTextLabel;
    @FXML private Label livesTitleLabel;
    @FXML private Label livesTextLabel;
    @FXML private Label scoreTitleLabel;
    @FXML private Label scoreTextLabel;
    @FXML private Label hintsTitleLabel;
    @FXML private Label hintsTextLabel;
    @FXML private Label surpriseTitleLabel;
    @FXML private Label surpriseTextLabel;
    @FXML private Label difficultyTitleLabel;
    @FXML private Label difficultyTextLabel;
    @FXML private Label profilesTitleLabel;
    @FXML private Label profilesTextLabel;
    @FXML private Label tipsTitleLabel;
    @FXML private Label tipsTextLabel;

    @FXML
    private void initialize() {
        SettingsController.applyThemeToRoot(rootPane);
        applyLanguage();
        playEntranceAnimations();
    }

    /**
     * Applies the current language (Hebrew or English) to all labels and buttons,
     * and adjusts node orientation for RTL/LTR layouts.
     */
    private void applyLanguage() {
        boolean heb = SettingsController.isHebrew();

        if (heb) {
            if (titleLabel != null) titleLabel.setText("איך משחקים?");
            if (closeButton != null) closeButton.setText("סגור");
            if (startButton != null) startButton.setText("התחלת המשחק 🦁");

            if (objectiveTitleLabel != null) objectiveTitleLabel.setText("🎯 מטרה");
            if (objectiveTextLabel != null)
                objectiveTextLabel.setText("• חשפו תאים בטוחים והימנעו ממוקשים.\n• לכל שחקן לוח משלו, ומשחקים בתור.\n• שמרו על הלבבות ונסו להגיע לניקוד הגבוה ביותר.");

            if (controlsTitleLabel != null) controlsTitleLabel.setText("🕹 שליטה");
            if (controlsTextLabel != null)
                controlsTextLabel.setText("• לחיצה שמאלית = חשיפת תא.\n• לחיצה ימנית = סימון או ביטול סימון של סימן סימבה/דגל (טביעת כף) על מוקשים חשודים.");

            if (numbersTitleLabel != null) numbersTitleLabel.setText("🔢 מספרים");
            if (numbersTextLabel != null)
                numbersTextLabel.setText("• המספר מראה כמה מוקשים צמודים לתא.");

            if (livesTitleLabel != null) livesTitleLabel.setText("❤️ חיים");
            if (livesTextLabel != null)
                livesTextLabel.setText("• מתחילים עם לבבות; פגיעה במוקש מורידה לב אחד.\n• כשנגמרים הלבבות — מפסידים.");

            if (scoreTitleLabel != null) scoreTitleLabel.setText("🏆 ניקוד / נקודות");
            if (scoreTextLabel != null)
                scoreTextLabel.setText("• חשיפות בטוחות והתקדמות יציבה מוסיפות נקודות.\n• סימון מוקש נכון מוסיף נקודות; סימון שגוי מוריד נקודות.\n• שאלות טריוויה יכולות להוסיף או להוריד נקודות (ההשפעה משתנה לפי הקושי).\n• מוקשים, הפתעות מסוכנות ושימוש ברמז מורידים נקודות.");

            if (hintsTitleLabel != null) hintsTitleLabel.setText("💡 רמזים");
            if (hintsTextLabel != null)
                hintsTextLabel.setText("• לכל שחקן יש בדיוק 2 רמזים בכל משחק.\n• לחצו על כפתור הרמז כדי להדגיש תא בטוח; הרמזים מוגבלים ועולים בנקודות.");

            if (surpriseTitleLabel != null) surpriseTitleLabel.setText("🎁 מערכת מתנות / הפתעות");
            if (surpriseTextLabel != null)
                surpriseTextLabel.setText("• Lucky Surprise יכולה לתת +חיים ו/או +נקודות.\n• Risky Surprise יכולה להוריד חיים/נקודות.");

            if (difficultyTitleLabel != null) difficultyTitleLabel.setText("🧭 קושי");
            if (difficultyTextLabel != null)
                difficultyTextLabel.setText("• Easy / Medium / Hard — יותר מוקשים = יותר קשה.");

            if (profilesTitleLabel != null) profilesTitleLabel.setText("👤 פרופילים");
            if (profilesTextLabel != null)
                profilesTextLabel.setText("• נכנסים לפרופיל מהמסך הראשי.\n• משנים אווטאר בתוך עמוד הפרופיל.\n• הפרופיל מציג היסטוריית משחקים וסטטיסטיקות.");

            if (tipsTitleLabel != null) tipsTitleLabel.setText("🏁 טיפים מהירים");
            if (tipsTextLabel != null)
                tipsTextLabel.setText("• השתמשו בדגלים כדי לעקוב אחרי מוקשים חשודים.\n• אל תמהרו — נקו אזורים בטוחים שלב אחר שלב.\n• שמרו רמזים לרגעים קשים.");

            // RTL
            if (rootPane != null)
                rootPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            if (contentCard != null) {
                contentCard.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                if (contentCard.getContent() != null) {
                    contentCard.getContent().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                }
            }

        } else {
            if (titleLabel != null) titleLabel.setText("How to Play?");
            if (closeButton != null) closeButton.setText("✕");
            if (startButton != null) startButton.setText("Got it, let's start! 🦁");

            if (rootPane != null)
                rootPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            if (contentCard != null) {
                contentCard.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                if (contentCard.getContent() != null) {
                    contentCard.getContent().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                }
            }
        }
    }

    private void playEntranceAnimations() {
        if (headerRow == null || contentCard == null || actionsRow == null) return;

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
        switchScene(event, "/view/home-view.fxml");
    }

    @FXML
    private void onStartGame(ActionEvent event) {
        switchScene(event, "/view/game-setup-view.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            var url = ResourceUtils.url(getClass(), fxmlPath);
            if (url == null) {
                return;
            }
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
}
