package controller;

import javafx.fxml.FXML;

import utility.viewSwitcher;




public  class SettingsController {
    //user clicks logout button, navigates to the sign-inscreen screen

    @FXML
    private void handleLogOut(){viewSwitcher.switchScene("sign-in.fxml");}
    @FXML
    private void handleBiteHistoryClick(){viewSwitcher.switchScene("bite-history.fxml");}



    @FXML
    private void handleScanScreenClick(){viewSwitcher.switchScene("scan-screen.fxml");}

    @FXML
    private void handleAboutBiteWiseClick() { viewSwitcher.switchScene("about-screen.fxml");}



    @FXML
    private void handleRecentBite(){viewSwitcher.switchScene("bite-history.fxml");}



}