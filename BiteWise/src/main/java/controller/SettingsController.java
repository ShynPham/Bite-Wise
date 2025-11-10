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
    private void handleLogOut(ActionEvent event) {
        try {
            Parent homeRoot = FXMLLoader.load(getClass().getResource("/edu/utsa/cs3443/group7/bitewise/sign-in.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }




        }
    }
