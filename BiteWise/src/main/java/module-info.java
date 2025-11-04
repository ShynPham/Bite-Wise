module edu.utsa.cs.group.bitewise {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.microsoft.onnxruntime;
    requires java.desktop;
    requires javafx.graphics;


    opens detectionApp to javafx.fxml;
    exports detectionApp;
    exports controller;
    opens controller to javafx.fxml;
    exports model;
    opens model to javafx.fxml;
    exports utility;
    opens utility to javafx.fxml;
}