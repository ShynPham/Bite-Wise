package controller;

import javafx.fxml.FXML;
import utility.viewSwitcher;


public class entryScreen {

    @FXML
    private void handleSignInClick(){
        viewSwitcher.switchScene("scan-screen.fxml");
    }

    public void shutdown() {
        InterferenceController.shutdown();
    }

}
