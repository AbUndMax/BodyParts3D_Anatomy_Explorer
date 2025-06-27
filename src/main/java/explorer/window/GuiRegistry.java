package explorer.window;

import explorer.window.controller.*;
import explorer.window.presenter.MainViewPresenter;
import explorer.window.presenter.SelectionViewPresenter;
import explorer.window.presenter.VisualizationViewPresenter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

public class GuiRegistry {
    private final Parent root;
    private final MainViewController mainViewController;
    private final SelectionViewController selectionViewController;
    private final VisualizationViewController visualizationViewController;

    // using presenter classes for each view respectively for better clarity and smaller individual presenter classes
    private final MainViewPresenter mainViewPresenter;
    private final SelectionViewPresenter selectionViewPresenter;
    private final VisualizationViewPresenter visualizationViewPresenter;

    public GuiRegistry() throws IOException {
        URL mainFXML = getClass().getResource("/fxml/MainView.fxml");
        FXMLLoader loader = new FXMLLoader(mainFXML);
        root = loader.load();

        mainViewController = loader.getController();
        selectionViewController = mainViewController.getSelectionViewController();
        visualizationViewController = mainViewController.getVisualizationViewController();
        mainViewPresenter = new MainViewPresenter(this);
        selectionViewPresenter = new SelectionViewPresenter(this);
        visualizationViewPresenter = new VisualizationViewPresenter(this);
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

    public MainViewPresenter getMainViewPresenter() {
        return mainViewPresenter;
    }

    public SelectionViewPresenter getSelectionViewPresenter() {
        return selectionViewPresenter;
    }

    public VisualizationViewPresenter getVisualizationViewPresenter() {
        return visualizationViewPresenter;
    }
}
