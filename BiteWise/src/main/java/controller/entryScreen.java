package controller;

import javafx.fxml.FXML;
import utility.viewSwitcher;


public class entryScreen {



    @FXML
    // Handle button click after either signing up
    private void handleContinueButtonClickAfterSignup(){viewSwitcher.switchScene("sign-in.fxml");}
    @FXML
    // Handle button after succesfully signing in
    private void scanScreenEntry(){viewSwitcher.switchScene("scan-screen.fxml");}


    public void shutdown() {
        InterferenceController.shutdown();
    }

}
