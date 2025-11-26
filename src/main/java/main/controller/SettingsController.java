package main.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.Objects;

public class SettingsController {

    @FXML
    private ToggleButton soundToggle;

    @FXML
    private ToggleButton fullscreenToggle;

    @FXML
    private Slider volumeSlider;

    private static Clip bgClip = null;
    private static boolean soundOn = false;
    private static double volume = 0.7;

    private void initClipIfNeeded() {
        if (bgClip != null) {
            return;
        }

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

        if (volumeSlider != null) {
            volumeSlider.setMin(0);
            volumeSlider.setMax(100);
            volumeSlider.setValue(volume * 100);
        }

        initClipIfNeeded();

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

        if (volumeSlider != null) {
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double v = newVal.doubleValue() / 100.0;
                applyVolume(v);
            });
        }

        if (fullscreenToggle != null) {
            fullscreenToggle.setSelected(false);
            fullscreenToggle.setText("OFF");

            fullscreenToggle.setOnAction(e -> {
                boolean full = fullscreenToggle.isSelected();
                fullscreenToggle.setText(full ? "ON" : "OFF");
            });
        }
    }

    @FXML
    private void closeSettings() {
        Stage stage = (Stage) soundToggle.getScene().getWindow();
        stage.close();
    }
}
