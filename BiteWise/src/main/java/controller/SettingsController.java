package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utility.viewSwitcher;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.prefs.Preferences;


public  class SettingsController {
    //user clicks logout button, navigates to the sign-inscreen screen

    @FXML
    private void handleLogOut(){viewSwitcher.switchScene("sign-in.fxml");}


    @FXML
    private void handleScanScreenClick(){viewSwitcher.switchScene("scan-screen.fxml");}

    @FXML
    private void handleAboutBiteWiseClick() { viewSwitcher.switchScene("about-screen.fxml");}

}