package explorer;

import explorer.window.ControllerRegistry;
import explorer.window.presenter.MainViewPresenter;
import explorer.window.presenter.SelectionViewPresenter;
import explorer.window.presenter.VisualizationViewPresenter;
import javafx.application.Application;
import javafx.stage.Stage;

public class AnatomyExplorer extends Application {

    public void start(Stage primaryStage) throws Exception {
        var registry = new ControllerRegistry();

        // using presenter classes for each view respectively for better clarity and smaller individual presenter classes
        new MainViewPresenter(registry);
        new SelectionViewPresenter(registry);
        new VisualizationViewPresenter(registry);

        primaryStage.setTitle("Anatomy Explorer");
        primaryStage.setScene(new javafx.scene.Scene(registry.getRoot(), 1000, 700));
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }
}
