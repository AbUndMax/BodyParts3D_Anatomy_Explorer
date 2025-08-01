module explorer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.esotericsoftware.kryo;
    requires org.objenesis;
    requires java.prefs;
    requires jdk.compiler;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.logging;

    opens explorer.window to javafx.fxml;
    exports explorer;
    opens explorer.window.controller to javafx.fxml;
    opens explorer.model.treetools to com.esotericsoftware.kryo, javafx.fxml;
    opens explorer.window.vistools to com.esotericsoftware.kryo;
    opens explorer.selection to com.esotericsoftware.kryo, javafx.fxml;
    opens explorer.model to com.esotericsoftware.kryo, javafx.fxml;
}