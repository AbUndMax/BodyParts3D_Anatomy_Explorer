package explorer;

import explorer.apptools.AppLogger;
import explorer.window.GuiRegistry;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;

public class AnatomyExplorer extends Application {

    public void start(Stage primaryStage) throws Exception {
        AppLogger.getLogger().info("Logger initialized");
        // set local exception handler since FXExceptions cannot be caught in catch phrases!
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            AppLogger.getLogger().log(Level.SEVERE, "Uncaught exception in thread " + thread.getName(), throwable);
        });

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
