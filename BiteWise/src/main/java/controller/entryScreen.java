package controller;

import javafx.fxml.FXML;
import utility.viewSwitcher;
import controller.InterferenceController;

import java.io.IOException;


public class entryScreen {

    @FXML
    private void handleSignInClick(){
        viewSwitcher.switchScene("scan-screen.fxml");
    }

    public void shutdown() {
        InterferenceController.shutdown();
    }

}
