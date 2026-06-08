package utility;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

/**
 * A utility class for switching scenes on the primary stage.
 * <p>
 * This class provides a method to smoothly, fancy way of transition between FXML scenes
 * using a fade-out, fade-in animation.
 *
 * @author Phu Pham
 */
public class viewSwitcher {

    /**
     * The reference to the primary application window (Stage).
     * This must be set once by the main application class.
     */
    public static Stage mainStage;

    /**
     * The duration (in milliseconds) for the fade-out and fade-in animations.
     */
    private static final double FADE_DURATION = 250; // Milliseconds for fade

    /**
     * Stores the primary application Stage for later use by the switchScene method.
     * This should be called once during application startup.
     *
     * @param stage The main Stage provided by the Application's start() method.
     */
    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    /**
     * Initiates a smooth, animated transition to a new scene.
     * It fades out the current scene, then loads and fades in the new one.
     *
     * @param fxmlFileName The simple name of the FXML file to load (e.g., "scan-screen.fxml").
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public static boolean switchScene(String fxmlFileName) {
        try {
            String fXMLPath = "/edu/utsa/cs3443/group7/bitewise/" + fxmlFileName;
            if (viewSwitcher.class.getResource(fXMLPath) == null) {
                System.err.println("FXML file not found: " + fxmlFileName);
                return false;
            }
            // --- FADE OUT THE OLD SCENE ---

            // Get the root node of the *current* scene
            FadeTransition fadeOut = getFadeTransition(fxmlFileName);

            // Start the fade-out animation
            fadeOut.play();
            return true;

        } catch (Exception e) { // Catch potential errors (e.g., mainStage not set)
            System.err.println("Error during scene switch (fade out): " + fxmlFileName);
            //old method for debugging
            e.printStackTrace();
            return false;
        }
    }

    private static FadeTransition getFadeTransition(String fxmlFileName) {
        Parent currentRoot = mainStage.getScene().getRoot();

        // Create the fade-out transition for the current root
        FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION), currentRoot);
        fadeOut.setFromValue(1.0); // Start fully opaque
        fadeOut.setToValue(0.0);   // End fully transparent

        // Define what to do *after* the fade-out animation finishes
        fadeOut.setOnFinished(event -> {
            // Now that the old scene is gone, load the new one
            loadAndFadeIn(fxmlFileName);
        });
        return fadeOut;
    }

    /**
     * Private helper method to load, set, and fade in the new scene.
     * This is called *after* the old scene has finished fading out.
     *
     * @param fxmlFileName The simple name of the FXML file to load.
     */
    private static void loadAndFadeIn(String fxmlFileName) {
        try {
            // 1. Load the new FXML
            // Build the absolute path from the 'resources' root
            String fXMLPath = "/edu/utsa/cs3443/group7/bitewise/" + fxmlFileName;
            // Load the FXML file. Objects.requireNonNull() ensures we get a NullPointerException
            // if the file path is wrong (which is caught below).
            Parent newRoot = FXMLLoader.load(Objects.requireNonNull(viewSwitcher.class.getResource(fXMLPath)));
            newRoot.setStyle(newRoot.getStyle() + "; -fx-font-family: 'KG Red Hands';");

            // 2. Set the new scene on the main stage
            mainStage.setScene(new Scene(newRoot));
            applyStageMode(fxmlFileName);

            // 3. Make the new root transparent (FIXED: was 1.0)
            // It must start at 0.0 opacity to fade *in*.
            newRoot.setOpacity(0.0);

            // 4. Create the fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION), newRoot);
            fadeIn.setFromValue(0.0); // Start fully transparent
            fadeIn.setToValue(1.0);   // End fully opaque

            // 5. Start the fade-in
            fadeIn.play();

        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load FXML file: " + fxmlFileName);
            // old method for debugging
            e.printStackTrace();
        }
    }

    private static void applyStageMode(String fxmlFileName) {
        boolean compactAuthScreen = fxmlFileName.equals("sign-in.fxml")
                || fxmlFileName.equals("sign-up.fxml")
                || fxmlFileName.equals("forgot-password.fxml");

        mainStage.setFullScreen(false);
        mainStage.setResizable(!compactAuthScreen);

        if (compactAuthScreen) {
            mainStage.setMaximized(false);
            mainStage.sizeToScene();
            mainStage.centerOnScreen();
            return;
        }

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        mainStage.setX(bounds.getMinX());
        mainStage.setY(bounds.getMinY());
        mainStage.setWidth(bounds.getWidth());
        mainStage.setHeight(bounds.getHeight());
        mainStage.setMaximized(true);
    }
}
