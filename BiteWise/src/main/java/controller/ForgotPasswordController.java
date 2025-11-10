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

    private final String CSV_FILE = "bitewise-users.csv";

    @FXML
    private void initialize() {
        signupLink.setOnAction(e -> handleSignUpLinkClick());
        submitButton.setOnAction(e -> handleResetPasswordClick());
    }

    /**
     * Handles the Reset Password button click.
     * Reads bitewise-users.csv, updates the matching email’s password, and saves changes.
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
            String header = reader.readLine(); // skip the first line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    if (parts[0].equals(email)) {
                        parts[1] = newPassword; // update password
                        emailFound = true;
                    }
                    users.add(parts);
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error reading user file.");
            e.printStackTrace();
            return;
        }

        if (!emailFound) {
            showAlert(Alert.AlertType.ERROR, "Email not found. Please check again.");
            return;
        }

        // Rewrite updated CSV file
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            writer.println("email,password");
            for (String[] u : users) {
                writer.println(u[0] + "," + u[1]);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error saving updated password.");
            e.printStackTrace();
            return;
        }

        // Update Preferences for SignInController compatibility
        Preferences prefs = Preferences.userRoot().node("BiteWiseUser");
        prefs.put("email", email);
        prefs.put("password", newPassword);

        showAlert(Alert.AlertType.INFORMATION, "Password reset successfully!");
        viewSwitcher.switchScene("sign-in.fxml");
    }

    /** Handles navigation back to the Sign Up screen. */
    private void handleSignUpLinkClick() {
        viewSwitcher.switchScene("sign-up.fxml");
    }

    /** Utility method to show alerts */
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
