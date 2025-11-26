package controller;

 import javafx.fxml.FXML;
 import javafx.scene.control.Alert;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.TextField;
 import utility.APIService;
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

         if (!isValidEmail(inputEmail)) {
             showAlert("Invalid email format.\nPlease use your registered address (e.g. user@gmail.com)");
             return;
         }

         try {
             APIService api = new APIService();
             int userId = api.signin(inputEmail, inputPassword);

             Preferences prefs = Preferences.userNodeForPackage(this.getClass());
             prefs.put("userId", String.valueOf(userId));
             prefs.put("email", inputEmail);

             viewSwitcher.switchScene("scan-screen.fxml");
         } catch (Exception e) {
             if (e.getMessage().equals("Invalid email or password.")) {
                 showAlert("Invalid email or password.");
             }
             else {
                 showAlert(e.getMessage());
             }
         }
     }

     private boolean isValidEmail(String email) {
         return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
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