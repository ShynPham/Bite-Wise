module edu.utsa.cs.group.bitewise {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.microsoft.onnxruntime;
    requires java.desktop;
    requires javafx.graphics;
    requires java.prefs;
    requires org.json;


    opens detectionApp to javafx.fxml, javafx.graphics;
    exports detectionApp;

    exports controller;
    opens controller to javafx.fxml;

    exports model;
    opens model to javafx.fxml, javafx.base;

    exports utility;
    opens utility to javafx.fxml;

    opens edu.utsa.cs3443.group7.bitewise to javafx.fxml;
}