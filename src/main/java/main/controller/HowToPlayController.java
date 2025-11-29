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

public class HowToPlayController {

    @FXML private AnchorPane rootPane;
    @FXML private HBox headerRow;
    @FXML private ScrollPane contentCard;
    @FXML private HBox actionsRow;

    @FXML private Label titleLabel;
    @FXML private Button closeButton;
    @FXML private Button startButton;

    // כל הטקסטים בפנים
    @FXML private Label objectiveTitleLabel;
    @FXML private Label objectiveTextLabel;

    @FXML private Label elementsTitleLabel;

    @FXML private Label bombsTitleLabel;
    @FXML private Label bombsTextLabel;

    @FXML private Label triviaTitleLabel;
    @FXML private Label triviaTextLabel;

    @FXML private Label surprisesTitleLabel;
    @FXML private Label surprisesTextLabel;

    @FXML private Label numbersTitleLabel;
    @FXML private Label numbersTextLabel;

    @FXML private Label howPlayTitleLabel;
    @FXML private Label step1Label;
    @FXML private Label step2Label;
    @FXML private Label step3Label;
    @FXML private Label step4Label;
    @FXML private Label step5Label;

    @FXML private Label tipsTitleLabel;
    @FXML private Label tip1Label;
    @FXML private Label tip2Label;
    @FXML private Label tip3Label;
    @FXML private Label tip4Label;

    @FXML
    private void initialize() {
        SettingsController.applyThemeToRoot(rootPane);
        applyLanguage();
        playEntranceAnimations();
    }

    private void applyLanguage() {
        boolean heb = SettingsController.isHebrew();

        if (heb) {
            // כותרת + כפתורים
            if (titleLabel != null) titleLabel.setText("איך משחקים?");
            if (closeButton != null) closeButton.setText("סגור");
            if (startButton != null) startButton.setText("התחלת המשחק 🦁");

            // טקסטים בעברית
            if (objectiveTitleLabel != null) objectiveTitleLabel.setText("🎯 מטרה");
            if (objectiveTextLabel != null)
                objectiveTextLabel.setText("לנקות את כל המשבצות הבטוחות בלי לפגוע במוקשים! לשני השחקנים יש לבבות משותפים – שימו לב ושמרו על החיים שלכם.");

            if (elementsTitleLabel != null) elementsTitleLabel.setText("📦 מרכיבי המשחק:");

            if (bombsTitleLabel != null) bombsTitleLabel.setText("💣 מוקשים");
            if (bombsTextLabel != null)
                bombsTextLabel.setText("היזהרו ממוקשים – חשיפת מוקש מורידה לב. סימון מוקש אמיתי עם דגל נותן נקודות.");

            if (triviaTitleLabel != null) triviaTitleLabel.setText("❓ שאלות טריוויה");
            if (triviaTextLabel != null)
                triviaTextLabel.setText("שאלות טריוויה נותנות פרס על תשובה נכונה וקנס על תשובה שגויה. ההשפעה משתנה לפי רמת הקושי.");

            if (surprisesTitleLabel != null) surprisesTitleLabel.setText("🎁 הפתעות");
            if (surprisesTextLabel != null)
                surprisesTextLabel.setText("משבצות הפתעה נותנות אפקט טוב או רע באקראי. הן יכולות להוסיף או להוריד לבבות או נקודות. כל הפתעה ניתנת לשימוש פעם אחת, והאפקט מושפע מרמת הקושי.");

            if (numbersTitleLabel != null) numbersTitleLabel.setText("🔢 מספרים");
            if (numbersTextLabel != null)
                numbersTextLabel.setText("המספרים מראים כמה מוקשים יש סביב המשבצת. השתמשו בהם כדי לתכנן את המהלכים שלכם.");

            if (howPlayTitleLabel != null) howPlayTitleLabel.setText("⚡ איך משחקים");
            if (step1Label != null) step1Label.setText("1. לכל שחקן יש לוח משלו והוא משחק בתורו.");
            if (step2Label != null) step2Label.setText("2. לחיצה שמאלית = גילוי משבצת.");
            if (step3Label != null) step3Label.setText("3. לחיצה ימנית = הוספה או הסרה של דגל על מוקש חשוד.");
            if (step4Label != null) step4Label.setText("4. נסו לשמור על הלבבות המשותפים בחיים כמה שיותר זמן.");
            if (step5Label != null) step5Label.setText("5. המנצח הוא השחקן שסימן נכון את כל המוקשים ראשון.");

            if (tipsTitleLabel != null) tipsTitleLabel.setText("🏁 טיפים לניצחון");
            if (tip1Label != null) tip1Label.setText("• השתמשו במספרים כדי להסיק איפה מסתתרים מוקשים.");
            if (tip2Label != null) tip2Label.setText("• תחשבו לפני שאתם לוחצים – טעות אחת יכולה לעלות בלב.");
            if (tip3Label != null) tip3Label.setText("• התכוננו לשאלות טריוויה כדי להרוויח נקודות נוספות.");
            if (tip4Label != null) tip4Label.setText("• הפתעות יכולות להפוך את המשחק – תהיו מוכנים!");

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
            // אנגלית – נשארים עם הטקסטים מה-FXML, רק כיוון
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
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
