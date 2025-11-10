package controller;

 import javafx.fxml.FXML;
 import javafx.scene.control.Alert;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.TextField;
 import utility.viewSwitcher;
 import java.util.prefs.Preferences;

 import java.io.BufferedWriter;
 import java.io.*;

 public class SignInController {

     @FXML private TextField emailField;
     @FXML private PasswordField passwordField;

     @FXML
     private void handleSignInButtonClick() {
         String inputEmail = emailField.getText();
         String inputPassword = passwordField.getText();

         if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
             showAlert("Please enter both email and password.");
             return;
         }

         // --- NEW CSV-READING LOGIC ---

         // 1. Get the file path used in SignupController to bitewise_user.csv
         String csvFileName = "bitewise_users.csv";
         String projectRoot = System.getProperty("user.dir");
         File csvFile = new File(projectRoot, csvFileName);

         // 2. Check if the file exists
         if (!csvFile.exists()) {
             showAlert("No user accounts file found. Please sign up first.");
             return;
         }

         boolean isAuthenticated = false; // Track login status

         // 3. Read the file
         try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {

             String line;
             reader.readLine(); // Skip the header row ("email,password")

             // Loop through every user in the file
             while ((line = reader.readLine()) != null) {
                 String[] parts = line.split(",");

                 // Check if the line is valid
                 if (parts.length == 2) {
                     String savedEmail = parts[0];
                     String savedPassword = parts[1];

                     // Check for a match
                     if (savedEmail.equals(inputEmail) && savedPassword.equals(inputPassword)) {
                         isAuthenticated = true; // Match found!
                         break; // Stop searching
                     }
                 }
             }

         } catch (IOException e) {
             showAlert("Error reading user file: " + e.getMessage());
             e.printStackTrace();
             return; // Stop if there was a file error
         }

         // 4. Check the flag is true or false
         if (isAuthenticated) {
             viewSwitcher.switchScene("scan-screen.fxml");
         } else {
             showAlert("Incorrect email or password.");
         }
     }

     @FXML
     private void handleSignUpLinkClick(){viewSwitcher.switchScene("sign-up.fxml");}

     @FXML
     private void handleForgotPasswordClick() {
         viewSwitcher.switchScene("forgot-password.fxml");
     }

     private void showAlert(String msg) {
         Alert alert = new Alert(Alert.AlertType.ERROR);
         alert.setTitle("Login failed.");
         alert.setHeaderText(null);
         alert.setContentText(msg);
         alert.showAndWait();
     }


     public void shutdown() {
         InterferenceController.shutdown();
     }
 }