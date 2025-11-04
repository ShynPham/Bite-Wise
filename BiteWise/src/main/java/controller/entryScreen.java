package controller;

import javafx.fxml.FXML;
import utility.viewSwitcher;


public class entryScreen {

    @FXML
    // Handle button when the SignIn button is click
    private void handleSignInClick(){
        viewSwitcher.switchScene("sign-in.fxml");
    }
    @FXML
    // Handle button when the SignUp button is click
    private void handleSignUpClick(){
        viewSwitcher.switchScene("sign-up.fxml");
    }
    @FXML
    // Handle button click after either signing in or up
    private void handleContinueButtonClick(){
        viewSwitcher.switchScene("scan-screen.fxml");
    }

    public void shutdown() {
        InterferenceController.shutdown();
    }

}
