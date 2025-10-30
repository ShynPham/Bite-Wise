module edu.utsa.cs3443.bitewise {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.utsa.cs3443.bitewise to javafx.fxml;
    exports edu.utsa.cs3443.bitewise;
}