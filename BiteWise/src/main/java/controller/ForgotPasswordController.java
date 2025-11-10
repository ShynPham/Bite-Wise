package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
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
     * Updates the user password in bitewise_users.csv and Preferences.
     */
    private void handleResetPasswordClick() {
        String email = emailField.getText().trim();
        String newPassword = newPasswordField.getText().trim();

        if (email.isEmpty() || newPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please fill in all fields.");
            return;
        }

        File csvFile = new File(System.getProperty("user.dir"), CSV_FILE);
        if (!csvFile.exists()) {
            showAlert(Alert.AlertType.ERROR, "No user data found. Please sign up first.");
            return;
        }

        boolean emailFound = false;
        List<String[]> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String header = reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String savedEmail = parts[0].trim().toLowerCase();
                    String typed = email.trim().toLowerCase();
                    String usernamePart = savedEmail.contains("@") ? savedEmail.substring(0, savedEmail.indexOf("@")) : savedEmail;
                    if (savedEmail.equals(typed) || usernamePart.equals(typed)) {
                        parts[1] = newPassword; // update password
                        emailFound = true;
                    }
                    users.add(parts);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error reading user file.");
            return;
        }

        if (!emailFound) {
            showAlert(Alert.AlertType.ERROR, "Email not found. Please check again.");
            return;
        }

        // Rewrite CSV file with updated data
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            writer.println("email,password");
            for (String[] user : users) {
                writer.println(user[0] + "," + user[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error saving updated password.");
            return;
        }

        // Update stored Preferences so SignInController stays in sync
        Preferences prefs = Preferences.userRoot().node("BiteWiseUser");
        prefs.put("email", email);
        prefs.put("password", newPassword);

        showAlert(Alert.AlertType.INFORMATION, "Password reset successful!");
        viewSwitcher.switchScene("sign-in.fxml");
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
}
