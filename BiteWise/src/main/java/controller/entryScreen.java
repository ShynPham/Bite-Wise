package controller;

import javafx.fxml.FXML;
import utility.viewSwitcher;

// dead stuff nobody use this lololol
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
