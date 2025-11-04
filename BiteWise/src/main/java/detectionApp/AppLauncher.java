package detectionApp;


import controller.entryScreen;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utility.viewSwitcher;

public class AppLauncher extends Application {

    private entryScreen controller; // Store the controller instance

    @Override
    public void start(Stage stage) throws Exception {
        viewSwitcher.setStage(stage);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/utsa/cs3443/group7/bitewise/sign-in.fxml"));
        Parent root = loader.load();

        // Get the controller instance from the loader
        controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setTitle("BiteWise");
        stage.setScene(scene);
        stage.setWidth(462);
        stage.setHeight(680);
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