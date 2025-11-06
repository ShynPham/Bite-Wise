package controller;

 import javafx.fxml.FXML;
 import javafx.scene.control.Alert;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.TextField;
 import utility.viewSwitcher;
 import java.util.prefs.Preferences;

 public class SignInController {

     @FXML private TextField emailField;
     @FXML private PasswordField passwordField;

     @FXML
     private void handleSignInClick() {
         Preferences prefs = Preferences.userRoot().node("BiteWiseUser");
         String savedEmail = prefs.get("email", "");
         String savedPassword = prefs.get("password", "");

         String inputEmail = emailField.getText();
         String inputPassword = passwordField.getText();

         if (inputEmail.equals(savedEmail) && inputPassword.equals(savedPassword)) {
             viewSwitcher.switchScene("menu-screen.fxml");
         } else {
             showAlert("Incorrect email or password.");
         }
     }

     @FXML
     private void handleSignUpLinkClick() {
         viewSwitcher.switchScene("sign-up.fxml");
     }

     @FXML
     private void handleForgotPasswordClick() {
         viewSwitcher.switchScene("forgot-password.fxml");
     }

     private void showAlert(string msg) {
         Alert alert = new Alert(Alert.AlertType.ERROR);
         alert.setTitle("Login failed.");
         alert.setHeaderText(null);
         alert.setContentText(msg);
         alert.showAndWait();
     }
 }