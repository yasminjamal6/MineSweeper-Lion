package main.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

public class SettingsController {

    @FXML
    private ToggleButton soundToggle;

    @FXML
    private ToggleButton fullscreenToggle;

    @FXML
    private Slider volumeSlider;

    @FXML
    private void initialize() {

        // סאונד – מצב התחלתי ON
        if (soundToggle != null) {
            soundToggle.setSelected(false);
            soundToggle.setText("ON");

            soundToggle.setOnAction(e -> {
                boolean off = soundToggle.isSelected();
                soundToggle.setText(off ? "OFF" : "ON");
                // כאן אפשר בהמשך לחבר למנהל סאונד
            });
        }

        // מסך מלא – מצב התחלתי OFF
        if (fullscreenToggle != null) {
            fullscreenToggle.setSelected(false);
            fullscreenToggle.setText("OFF");

            fullscreenToggle.setOnAction(e -> {
                boolean on = fullscreenToggle.isSelected();
                fullscreenToggle.setText(on ? "ON" : "OFF");
                // כאן אפשר בהמשך לחבר לפול-סקרין
            });
        }

        if (volumeSlider != null) {
            volumeSlider.setMin(0);
            volumeSlider.setMax(100);
            volumeSlider.setValue(70);
        }
    }

    @FXML
    private void closeSettings() {
        Stage stage = (Stage) soundToggle.getScene().getWindow();
        stage.close();

    }
}
