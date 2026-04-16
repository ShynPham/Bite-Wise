package detectionApp;


import controller.SignInController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import utility.viewSwitcher;
import javafx.scene.image.Image;

import java.io.InputStream;

public class AppLauncher extends Application {

    private SignInController controller; // Store the controller instance

    @Override
    public void start(Stage stage) throws Exception {
        viewSwitcher.setStage(stage);
        // --- DEBUG FONT LOADING ---
        try (InputStream fontStream = getClass().getResourceAsStream("/edu/utsa/cs3443/group7/bitewise/fonts/KGRedHands.ttf")) {

            if (fontStream != null) {
                // Load the font and save the object
                Font loadedFont = Font.loadFont(fontStream, 12);

                if (loadedFont != null) {
                    // Print its real names to the console for debugging purposed
                    System.out.println("--- FONT LOADED SUCCESSFULLY ---");
                    System.out.println("Use this name in CSS: " + loadedFont.getFamily());
                    System.out.println(" (Full PostScript Name: " + loadedFont.getName() + ")");
                    System.out.println("---------------------------------");
                } else {
                    System.err.println("Error: Font file was found, but FAILED TO LOAD.");
                }
            } else {
                // This is the most likely error if it fails
                System.err.println("Error: Custom font 'KGRedHands.ttf' NOT FOUND.");
                System.err.println("Check your file path in AppLauncher.java!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        stage.setResizable(false);
        stage.show();
    }

    /**
     * This method is called when the application should stop.
     */
    @Override
    public void stop() throws Exception {
        if (controller != null) {
            controller.shutdown(); // shut the damnm thing down
        }
        super.stop();
    }

}