package main.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.collections.ObservableList;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.Objects;

public class SettingsController {

    @FXML
    private ToggleButton soundToggle;

    @FXML
    private ToggleButton fullscreenToggle;   // אם אין ב-FXML זה פשוט יהיה null

    @FXML
    private ToggleButton themeToggle;        // כפתור LIGHT / DARK

    @FXML
    private Slider volumeSlider;

    @FXML
    private Parent settingsRoot; // ה-Root של חלון ההגדרות (BorderPane מה-FXML)

    private static Clip bgClip = null;
    private static boolean soundOn = false;
    private static double volume = 0.7;

    // false = LIGHT, true = DARK
    private static boolean darkMode = false;

    /* ---- חשוף לשאר הקונטרולרים ---- */

    public static boolean isDarkMode() {
        return darkMode;
    }

    /** החלת ה־Theme על Root אחד (מסך אחד) */
    public static void applyThemeToRoot(Parent root) {
        if (root == null) return;

        ObservableList<String> styles = root.getStyleClass();

        // להסיר קודם
        styles.remove("dark-mode");

        // אם Dark Mode פעיל – להוסיף
        if (darkMode && !styles.contains("dark-mode")) {
            styles.add("dark-mode");
        }
    }

    /** החלת ה־Theme על כל החלונות הפתוחים */
    public static void applyThemeToAllWindows() {
        for (Window window : Window.getWindows()) {
            Scene scene = window.getScene();
            if (scene != null) {
                applyThemeToRoot(scene.getRoot());
            }
        }
    }

    /* ---- מוזיקה ---- */

    private void initClipIfNeeded() {
        if (bgClip != null) return;

        try {
            URL url = Objects.requireNonNull(
                    getClass().getResource("/sound/CircleoflifeLK.wav"),
                    "Background music file not found"
            );

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

    @FXML
    private void initialize() {

        // להחיל Theme על חלון ההגדרות עצמו לפי המצב הנוכחי
        applyThemeToRoot(settingsRoot);

        // ווליום
        if (volumeSlider != null) {
            volumeSlider.setMin(0);
            volumeSlider.setMax(100);
            volumeSlider.setValue(volume * 100);

            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double v = newVal.doubleValue() / 100.0;
                applyVolume(v);
            });
        }

        initClipIfNeeded();

        // סאונד ON/OFF
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

        // FULLSCREEN – אופציונלי
        if (fullscreenToggle != null) {
            fullscreenToggle.setSelected(false);
            fullscreenToggle.setText("OFF");
            fullscreenToggle.setOnAction(e -> {
                boolean full = fullscreenToggle.isSelected();
                fullscreenToggle.setText(full ? "ON" : "OFF");
                // אפשר להוסיף בהמשך stage.setFullScreen(full);
            });
        }

        // כפתור Theme (Light / Dark)
        if (themeToggle != null) {
            themeToggle.setSelected(darkMode);
            themeToggle.setText(darkMode ? "DARK" : "LIGHT");

            themeToggle.setOnAction(e -> {
                darkMode = themeToggle.isSelected();
                themeToggle.setText(darkMode ? "DARK" : "LIGHT");

                // להחיל על כל החלונות
                applyThemeToAllWindows();
            });
        }
    }

    @FXML
    private void closeSettings() {
        Stage stage = (Stage) soundToggle.getScene().getWindow();
        stage.close();
    }
}
