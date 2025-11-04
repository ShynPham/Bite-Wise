package detectionApp;


import controller.entryScreen;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utility.viewSwitcher;
import javafx.scene.image.Image;

import java.io.InputStream;

public class AppLauncher extends Application {

    private entryScreen controller; // Store the controller instance

    @Override
    public void start(Stage stage) throws Exception {
        viewSwitcher.setStage(stage);
        InputStream appIconStream = getClass().getResourceAsStream("/edu/utsa/cs3443/group7/bitewise/ui_icons/logo.png");
        if (appIconStream != null){
            Image appIcon = new Image(appIconStream);
            stage.getIcons().add(appIcon);
        } else {
            System.err.println("Error: Cannot find Application icon.");
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/utsa/cs3443/group7/bitewise/sign-in.fxml"));
        Parent root = loader.load();

        // Get the controller instance from the loader
        controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setTitle("BiteWise");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * This method is called when the application should stop.
     */
    @Override
    public void stop() throws Exception {
        if (controller != null) {
            controller.shutdown(); // Gracefully shut down the model and threads
        }
        super.stop();
    }

}