module com.imgmanip {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.desktop;
    requires transitive javafx.graphics;

    opens com.imgmanip to javafx.fxml;

    exports com.imgmanip;
}
