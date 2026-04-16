/**
 Controller for the about screen. Contains methods for back to settings button, and get started button
**/

package controller;

import javafx.fxml.FXML;
import utility.viewSwitcher;

public class AboutController {

    @FXML
    private void handleBackToSettings(){viewSwitcher.switchScene("settings-screen.fxml");}

    @FXML
    private void handleGetStarted(){viewSwitcher.switchScene("scan-screen.fxml");}
}