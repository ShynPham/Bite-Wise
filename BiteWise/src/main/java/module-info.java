module edu.utsa.cs3443.group7.bitewise {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.utsa.cs3443.group7.bitewise to javafx.fxml;
    exports edu.utsa.cs3443.group7.bitewise;
}