package explorer.window;

import explorer.window.controller.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.net.URL;

public class ControllerRegistry {
    private final Parent root;
    private final MainViewController mainViewController;
    private final SelectionViewController selectionViewController;
    private final VisualizationViewController visualizationViewController;

    public ControllerRegistry() throws IOException {
        URL mainFXML = getClass().getResource("/fxml/MainView.fxml");
        FXMLLoader loader = new FXMLLoader(mainFXML);
        root = loader.load();
        mainViewController = loader.getController();
        selectionViewController = mainViewController.getSelectionViewController();
        visualizationViewController = mainViewController.getVisualizationViewController();
    }

    public Parent getRoot() {
        return root;
    }

    public MainViewController getMainViewController() {
        return mainViewController;
    }

    public SelectionViewController getSelectionViewController() {
        return selectionViewController;
    }

    public VisualizationViewController getVisualizationViewController() {
        return visualizationViewController;
    }
}
