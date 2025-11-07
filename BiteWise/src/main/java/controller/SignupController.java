package controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import utility.viewSwitcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

public class SignupController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleSignUpClickEvent() throws IOException {
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
        exportToCSV(email, password);
        // navigate to log in screen
        viewSwitcher viewSwitch;
        viewSwitcher.switchScene("sign-in.fxml");
    }

    private void exportToCSV(String email, String password) throws IOException {
        String csvFileName = "bitewise_users.csv";

        // "user.dir" gets the directory where the application was launched
        String projectRoot = System.getProperty("user.dir");
        File csvFile = new File(projectRoot, csvFileName);
        // ------------------------------
        // Check if the file is new so we can add a header
        boolean isNewFile = !csvFile.exists();

        // Use try-with-resources to automatically close the writers
        try (FileWriter fw = new FileWriter(csvFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            if (isNewFile) {
                out.println("email,password");
            }

            // Write the new user data
            out.println(email + "," + password);
        }
    }

    private void showAlert(String msg) {
        Alert alert;
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("BiteWise");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    @FXML
    private void handleSignInClickEvent(){viewSwitcher.switchScene("sign-in.fxml");}



    public void shutdown() {
        InterferenceController.shutdown();
    }
}