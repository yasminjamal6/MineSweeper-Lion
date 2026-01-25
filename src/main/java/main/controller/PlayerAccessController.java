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

    /** Indicates whether access was successfully granted */
    private boolean accessGranted = false;


    /**
     * Initializes UI state after FXML loading.
     * - Hides error label by default.
     * - Clears error message when user input changes.
     */
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

   // Initializes the controller with its owning stage and expected username.
    public void init(Stage stage, String expectedUsername) {
        this.stage = stage;
        this.expectedUsername = expectedUsername;
    }

    public boolean isAccessGranted() {
        return accessGranted;
    }

    /**
     * Handles the Cancel button action.
     * Closes the dialog without granting access.
     */
    @FXML
    private void onCancel() {
        accessGranted = false;
        if (stage != null) stage.close();
    }

    /**
     * Handles the Enter/Confirm button action.
     * Grants access if the entered username matches the expected one.
     */
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

    //Displays an error message in the UI.
    private void showError(String msg) {
        if (errorLabel == null) return;
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    // Hides the error message from the UI.
    private void hideError() {
        if (errorLabel == null) return;
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
