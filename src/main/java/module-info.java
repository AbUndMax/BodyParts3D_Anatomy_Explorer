module explorer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;

    opens explorer.window to javafx.fxml;
    exports explorer;
    opens explorer.window.controller to javafx.fxml;
}