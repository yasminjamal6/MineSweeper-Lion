package main.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.ColorAdjust;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.sound.sampled.*;
import java.net.URL;
import main.util.ResourceUtils;
import javafx.geometry.NodeOrientation;


public class SettingsController {

    @FXML
    private ToggleButton soundToggle;

    @FXML
    private ToggleButton fullscreenToggle;

    @FXML
    private ToggleButton themeToggle;

    @FXML
    private ToggleButton languageToggle;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Slider brightnessSlider;

    @FXML
    private Parent settingsRoot;

    private static Clip bgClip = null;
    private static boolean soundOn = false;
    private static double volume = 0.7;
    private static boolean darkMode = false;
    private static boolean hebrew = false;
    private static double brightnessLevel = 0.0;


    public static boolean isDarkMode() {
        return darkMode;
    }

    public static boolean isHebrew() {
        return hebrew;
    }

    public static void applyThemeToRoot(Parent root) {
        if (root == null) return;

        ObservableList<String> styles = root.getStyleClass();
        styles.remove("dark-mode");

        if (darkMode && !styles.contains("dark-mode")) {
            styles.add("dark-mode");
        }

        applyBrightnessToRoot(root);
    }

    public static void applyThemeToAllWindows() {
        for (Window window : Window.getWindows()) {
            Scene scene = window.getScene();
            if (scene != null) {
                applyThemeToRoot(scene.getRoot());
            }
        }
    }

    public static void applyBrightnessToRoot(Parent root) {
        if (root == null) return;
        ColorAdjust ca = new ColorAdjust();
        ca.setBrightness(brightnessLevel);
        root.setEffect(ca);
    }

    public static void applyBrightnessToAllWindows() {
        for (Window window : Window.getWindows()) {
            Scene scene = window.getScene();
            if (scene != null) {
                applyBrightnessToRoot(scene.getRoot());
            }
        }
    }

    /** משנה טקסטים *בכל* החלונות לפי השפה שנבחרה */
    public static void refreshLanguageOnAllWindows() {
        for (Window window : Window.getWindows()) {
            Scene scene = window.getScene();
            if (scene == null) continue;

            Parent root = scene.getRoot();
// אם זה חלון ההגדרות – להפוך כיוון לימין-לשמאל בעברית
            Parent settingsRootNode = (Parent) root.lookup("#settingsRoot");
            if (settingsRootNode != null) {
                settingsRootNode.setNodeOrientation(
                        hebrew ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT
                );
            }

            // ----- מסך הבית -----
            Label title = (Label) root.lookup("#titleLabel");
            Label subtitle = (Label) root.lookup("#subtitleLabel");
            Button startBtn = (Button) root.lookup("#startButton");
            Button questionBtn = (Button) root.lookup("#questionManagerButton");
            Button historyBtn = (Button) root.lookup("#historyButton");
            Button howBtn = (Button) root.lookup("#howButton");

            // ----- חלון ההגדרות -----
            Label settingsTitle = (Label) root.lookup("#settingsTitleLabel");
            Label soundLabel = (Label) root.lookup("#soundLabel");
            Label themeLabel = (Label) root.lookup("#themeLabel");
            Label languageLabel = (Label) root.lookup("#languageLabel");
            Label volumeLabel = (Label) root.lookup("#volumeLabel");
            Label brightnessLabel = (Label) root.lookup("#brightnessLabel");
            Button closeSettingsBtn = (Button) root.lookup("#closeSettingsBtn");

            // ===== מסך ה-Game Setup =====
            Parent gameSetupRoot = (Parent) root.lookup("#gameSetupRoot");
            if (gameSetupRoot != null) {
                gameSetupRoot.setNodeOrientation(
                        hebrew ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT
                );
            }

            // setup
            Label setupTitleLabel      = (Label) root.lookup("#setupTitleLabel");
            Label setupSubtitleLabel   = (Label) root.lookup("#setupSubtitleLabel");
            Label playerATitleLabel    = (Label) root.lookup("#playerATitleLabel");
            Label playerANameLabel     = (Label) root.lookup("#playerANameLabel");
            Label playerABoardLabel    = (Label) root.lookup("#playerABoardLabel");
            Label playerBTitleLabel    = (Label) root.lookup("#playerBTitleLabel");
            Label playerBNameLabel     = (Label) root.lookup("#playerBNameLabel");
            Label playerBBoardLabel    = (Label) root.lookup("#playerBBoardLabel");
            Label challengeTitleLabel  = (Label) root.lookup("#challengeTitleLabel");

            ToggleButton easyBtnNode   = (ToggleButton) root.lookup("#easyBtn");
            ToggleButton mediumBtnNode = (ToggleButton) root.lookup("#mediumBtn");
            ToggleButton hardBtnNode   = (ToggleButton) root.lookup("#hardBtn");

            Button startAdventureButton= (Button) root.lookup("#startAdventureButton");
            Button backToHomeButton    = (Button) root.lookup("#backToHomeButton");




            if (hebrew) {
                // בית
                if (title != null)      title.setText("מיינסוויפר חכם");
                if (subtitle != null)   subtitle.setText("🌟 טריוויה אגדה מהסוואנה! 🌟");
                if (startBtn != null)   startBtn.setText("התחל הרפתקה");
                if (questionBtn != null)questionBtn.setText("מנהל שאלות");
                if (historyBtn != null) historyBtn.setText("היסטוריית משחק");
                if (howBtn != null)     howBtn.setText("איך משחקים?");

                // הגדרות
                if (settingsTitle != null)   settingsTitle.setText("הגדרות ⚙");
                if (soundLabel != null)      soundLabel.setText("צליל:");
                if (themeLabel != null)      themeLabel.setText("ערכת נושא:");
                if (languageLabel != null)   languageLabel.setText("שפה:");
                if (volumeLabel != null)     volumeLabel.setText("עוצמת קול");
                if (brightnessLabel != null) brightnessLabel.setText("בהירות");
                if (closeSettingsBtn != null) closeSettingsBtn.setText("סגור");

                // ---- Game Setup – עברית ----
                if (setupTitleLabel != null)     setupTitleLabel.setText("הכנת המאורה");
                if (setupSubtitleLabel != null)  setupSubtitleLabel.setText("הכינו את האריות לאתגר!");
                if (playerATitleLabel != null)   playerATitleLabel.setText("שחקן א' 🦁");
                if (playerANameLabel != null)    playerANameLabel.setText("שם שחקן");
                if (playerABoardLabel != null)   playerABoardLabel.setText("סגנון לוח");

                if (playerBTitleLabel != null)   playerBTitleLabel.setText("שחקן ב' 🦁");
                if (playerBNameLabel != null)    playerBNameLabel.setText("שם שחקן");
                if (playerBBoardLabel != null)   playerBBoardLabel.setText("סגנון לוח");

                if (challengeTitleLabel != null) challengeTitleLabel.setText("בחרו את רמת האתגר");

                if (easyBtnNode != null)         easyBtnNode.setText("קל");
                if (mediumBtnNode != null)       mediumBtnNode.setText("בינוני");
                if (hardBtnNode != null)         hardBtnNode.setText("קשה");

                if (startAdventureButton != null)startAdventureButton.setText("התחל את ההרפתקה 🦁");
                if (backToHomeButton != null)    backToHomeButton.setText("חזרה לבית");


            } else {
                // בית
                if (title != null)      title.setText("Mine Sweeper Smart");
                if (subtitle != null)   subtitle.setText("🌟 Legendary trivia from the savanna! 🌟");
                if (startBtn != null)   startBtn.setText("Start Adventure");
                if (questionBtn != null)questionBtn.setText("Question Manager");
                if (historyBtn != null) historyBtn.setText("Game History");
                if (howBtn != null)     howBtn.setText("How to Play?");

                // הגדרות
                if (settingsTitle != null)   settingsTitle.setText("Settings ⚙");
                if (soundLabel != null)      soundLabel.setText("Sound:");
                if (themeLabel != null)      themeLabel.setText("Theme:");
                if (languageLabel != null)   languageLabel.setText("Language:");
                if (volumeLabel != null)     volumeLabel.setText("Volume");
                if (brightnessLabel != null) brightnessLabel.setText("Brightness");
                if (closeSettingsBtn != null) closeSettingsBtn.setText("Close");


                // ---- Game Setup – English ----
                if (setupTitleLabel != null)     setupTitleLabel.setText("Prepare the Den");
                if (setupSubtitleLabel != null)  setupSubtitleLabel.setText("Get your lions ready for the challenge!");
                if (playerATitleLabel != null)   playerATitleLabel.setText("Player A 🦁");
                if (playerANameLabel != null)    playerANameLabel.setText("Player name");
                if (playerABoardLabel != null)   playerABoardLabel.setText("Board style");

                if (playerBTitleLabel != null)   playerBTitleLabel.setText("Player B 🦁");
                if (playerBNameLabel != null)    playerBNameLabel.setText("Player name");
                if (playerBBoardLabel != null)   playerBBoardLabel.setText("Board style");

                if (challengeTitleLabel != null) challengeTitleLabel.setText("Choose the challenge level");

                if (easyBtnNode != null)         easyBtnNode.setText("Easy");
                if (mediumBtnNode != null)       mediumBtnNode.setText("Medium");
                if (hardBtnNode != null)         hardBtnNode.setText("Hard");

                if (startAdventureButton != null)startAdventureButton.setText("Start the Adventure 🦁");
                if (backToHomeButton != null)    backToHomeButton.setText("Back to Home");
            }
        }
    }

    /* ---------- מוזיקה ---------- */

    private void initClipIfNeeded() {
        if (bgClip != null) return;

        try {
            URL url = ResourceUtils.url(getClass(), "/sound/CircleoflifeLK.wav");
            if (url == null) {
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            bgClip = AudioSystem.getClip();
            bgClip.open(ais);

            bgClip.loop(Clip.LOOP_CONTINUOUSLY);
            applyVolume(volume);

            if (!soundOn) {
                bgClip.stop();
            } else {
                bgClip.start();
            }

        } catch (Exception e) {
            System.out.println("ERROR loading WAV file");
            e.printStackTrace();
        }
    }

    private void applyVolume(double v) {
        volume = v;

        if (bgClip == null) return;

        if (bgClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl =
                    (FloatControl) bgClip.getControl(FloatControl.Type.MASTER_GAIN);

            float min = -40.0f;
            float max = 0.0f;
            float gain = (float) (min + (volume * (max - min)));

            gain = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), gain));
            gainControl.setValue(gain);
        }
    }

    /* ---------- עזר לכפתור שפה ---------- */

    private void updateLanguageToggleText() {
        if (languageToggle != null) {
            languageToggle.setText(hebrew ? "עברית" : "English");
        }
    }

    /* ---------- initialize ---------- */

    @FXML
    private void initialize() {

        applyThemeToRoot(settingsRoot);
        initClipIfNeeded();

        // Volume
        if (volumeSlider != null) {
            volumeSlider.setMin(0);
            volumeSlider.setMax(100);
            volumeSlider.setValue(volume * 100);

            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double v = newVal.doubleValue() / 100.0;
                applyVolume(v);
            });
        }

        // Sound ON/OFF
        if (soundToggle != null) {
            soundToggle.setSelected(soundOn);
            soundToggle.setText(soundOn ? "ON" : "OFF");

            soundToggle.setOnAction(e -> {
                soundOn = soundToggle.isSelected();
                soundToggle.setText(soundOn ? "ON" : "OFF");

                if (bgClip != null) {
                    if (soundOn) {
                        bgClip.start();
                        bgClip.loop(Clip.LOOP_CONTINUOUSLY);
                    } else {
                        bgClip.stop();
                    }
                }
            });
        }

        // Fullscreen (כרגע רק טקסט)
        if (fullscreenToggle != null) {
            fullscreenToggle.setSelected(false);
            fullscreenToggle.setText("OFF");
            fullscreenToggle.setOnAction(e -> {
                boolean full = fullscreenToggle.isSelected();
                fullscreenToggle.setText(full ? "ON" : "OFF");
            });
        }

        // Theme
        if (themeToggle != null) {
            themeToggle.setSelected(darkMode);
            themeToggle.setText(darkMode ? "DARK" : "LIGHT");

            themeToggle.setOnAction(e -> {
                darkMode = themeToggle.isSelected();
                themeToggle.setText(darkMode ? "DARK" : "LIGHT");
                applyThemeToAllWindows();
            });
        }

        // Language
        if (languageToggle != null) {
            languageToggle.setSelected(hebrew);
            updateLanguageToggleText();

            languageToggle.setOnAction(e -> {
                hebrew = languageToggle.isSelected();
                updateLanguageToggleText();
                refreshLanguageOnAllWindows();
            });
        }

        // Brightness
        if (brightnessSlider != null) {
            brightnessSlider.setMin(0);
            brightnessSlider.setMax(100);

            double sliderVal = (brightnessLevel + 0.5) * 100.0;
            brightnessSlider.setValue(sliderVal);

            brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double norm = newVal.doubleValue() / 100.0; // 0..1
                brightnessLevel = norm - 0.5;               // -0.5..0.5
                applyBrightnessToAllWindows();
            });
        }

        // כשנכנסים להגדרות – לעדכן טקסטים לפי השפה הנוכחית
        refreshLanguageOnAllWindows();
    }

    @FXML
    private void closeSettings() {
        Stage stage = (Stage) soundToggle.getScene().getWindow();
        stage.close();
    }
}
