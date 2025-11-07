package controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import utility.viewSwitcher;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

public class SignupController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleSignup(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Please fill in all fields.");
            return;
        }

        Preferences prefs = Preferences.userRoot().node("BiteWiseUser");
        prefs.put("email", email);
        prefs.put("password", password);

        showAlert("Account created successfully!");
        // navigate to log in screen
        viewSwitcher viewSwitch;
        viewSwitcher.switchScene("sign-in.fxml");
    }

    private void showAlert(String msg) {
        Alert alert;
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("BiteWise");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}