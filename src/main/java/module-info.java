module explorer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires com.esotericsoftware.kryo;
    requires org.objenesis;
    requires java.prefs;
    requires jdk.compiler;

    opens explorer.window to javafx.fxml;
    exports explorer;
    opens explorer.window.controller to javafx.fxml;
    opens explorer.model to com.esotericsoftware.kryo;
    opens explorer.model.treetools to com.esotericsoftware.kryo, javafx.fxml;
    opens explorer.window.vistools to com.esotericsoftware.kryo;
    opens explorer.window.selection to com.esotericsoftware.kryo, javafx.fxml;
}