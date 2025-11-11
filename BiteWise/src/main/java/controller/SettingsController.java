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
    private void handleLogOut(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/edu/utsa/cs3443/group7/bitewise/sign-in.fxml"
            ));
            Parent settingsRoot = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            Scene scene = new Scene(settingsRoot);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load settings-screen.fxml");
        }
    }

    @FXML
    private void handleScanScreenClick(){viewSwitcher.switchScene("scan-screen.fxml");}

    @FXML
    private void handleBiteHistoryClick(){viewSwitcher.switchScene("bite-history.fxml");}
    @FXML
    private void handleDownload(javafx.event.ActionEvent actionEvent) {
        // You can leave it empty if you don't need it yet
        System.out.println("Download clicked");
    }

}