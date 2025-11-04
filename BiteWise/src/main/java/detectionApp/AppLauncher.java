package detectionApp;

import controller.InterferenceController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AppLauncher extends Application {

    private InterferenceController controller; // Store the controller instance

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/utsa/cs3443/group7/bitewise/scan-screen.fxml"));
        Parent root = loader.load();

        // Get the controller instance from the loader
        controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setTitle("YOLO ONNX - JavaFX");
        stage.setScene(scene);
        stage.setWidth(920);
        stage.setHeight(760);
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

    public static void main(String[] args) {
        launch(args);
    }
}