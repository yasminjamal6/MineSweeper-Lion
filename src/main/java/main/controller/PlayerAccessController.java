package main.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class PlayerAccessController {

    @FXML private PasswordField usernameField;
    @FXML private Label errorLabel;

    private Stage stage;
    private String expectedUsername;
    private boolean accessGranted = false;

    @FXML
    private void initialize() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        if (usernameField != null) {
            usernameField.textProperty().addListener((obs, oldV, newV) -> hideError());
        }
    }

    public void init(Stage stage, String expectedUsername) {
        this.stage = stage;
        this.expectedUsername = expectedUsername;
    }

    public boolean isAccessGranted() {
        return accessGranted;
    }

    @FXML
    private void onCancel() {
        accessGranted = false;
        if (stage != null) stage.close();
    }

    @FXML
    private void onEnter() {
        String input = usernameField != null ? usernameField.getText() : "";
        if (expectedUsername != null && input != null && input.equalsIgnoreCase(expectedUsername)) {
            accessGranted = true;
            if (stage != null) stage.close();
            return;
        }
        showError("Incorrect username.");
    }

    private void showError(String msg) {
        if (errorLabel == null) return;
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        if (errorLabel == null) return;
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
