package explorer;

import explorer.window.GuiRegistry;
import javafx.application.Application;
import javafx.stage.Stage;

public class AnatomyExplorer extends Application {

    public void start(Stage primaryStage) throws Exception {
        var registry = new GuiRegistry();

        primaryStage.setTitle("Anatomy Explorer");
        primaryStage.setScene(new javafx.scene.Scene(registry.getRoot(), 1000, 700));
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }
}
