package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import utility.APIService;
import utility.viewSwitcher;

import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private Hyperlink signupLink;
    @FXML private Button submitButton;

    private final String CSV_FILE = "bitewise_users.csv";

    @FXML
    private void initialize() {
        // Hook up button and link events when screen loads
        if (signupLink != null) {
            signupLink.setOnAction(e -> handleSignUpLinkClick());
        }
        if (submitButton != null) {
            submitButton.setOnAction(e -> handleResetPasswordClick());
        }
    }

    /**
     * Handles the reset password logic.
     */
    @FXML
    private void handleResetPasswordClick() {
        String email = emailField.getText();
        String newPassword = newPasswordField.getText();

        if (!validated(email, newPassword)) {
            return;
        }

        resetPassword(email, newPassword);
    }

    private void resetPassword(String email, String newPassword) {
        try {
            APIService api = new APIService();
            api.changePassword(email, newPassword);

            showAlert(Alert.AlertType.INFORMATION, "Password reset successful!");
            viewSwitcher.switchScene("sign-in.fxml");
        } catch (Exception e) {
            if (e.getMessage().equals("Invalid email")) {
                showAlert(Alert.AlertType.ERROR, "Invalid email!");
            }
            else {
                showAlert(Alert.AlertType.ERROR, "Error resetting password!");
            }
        }
    }

    private boolean validated(String email, String newPassword) {
        if (email.isEmpty() || newPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please fill in all fields.");
            return false;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid email format.\nPlease enter a valid email like:\nuser@gmail.com or user@yahoo.com");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

    /** Navigate to Sign Up screen when link clicked */
    private void handleSignUpLinkClick() {
        viewSwitcher.switchScene("sign-up.fxml");
    }

    /** Show alert popups with custom messages */
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("BiteWise");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void shutdown() {
        InterferenceController.shutdown();
    }

    public void handleSignInLinkClick() { viewSwitcher.switchScene("sign-in.fxml");
    }
}
