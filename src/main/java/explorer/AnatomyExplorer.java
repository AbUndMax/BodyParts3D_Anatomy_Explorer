package explorer;

import explorer.window.GuiRegistry;
import javafx.application.Application;
import javafx.stage.Stage;

public class AnatomyExplorer extends Application {

    public void start(Stage primaryStage) throws Exception {
        var registry = new GuiRegistry();

        primaryStage.setTitle("Anatomy Explorer");
        primaryStage.setScene(registry.getMainScene());

        primaryStage.setMinWidth(820);
        primaryStage.setWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.setHeight(700);

        // set lightMode by default
        registry.getMainViewController().getLightModeMenuItem().fire();

        primaryStage.show();
    }
}
