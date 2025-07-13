package explorer.window;

import explorer.window.command.CommandManager;
import explorer.window.controller.*;
import explorer.window.presenter.MainViewPresenter;
import explorer.window.presenter.SelectionViewPresenter;
import explorer.window.presenter.VisualizationViewPresenter;
import explorer.window.selection.SelectionBinder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;

public class GuiRegistry {
    private final Parent root;
    private final Scene mainScene;
    private final MainViewController mainViewController;
    private final SelectionViewController selectionViewController;
    private final VisualizationViewController visualizationViewController;

    // using presenter classes for each view respectively for better clarity and smaller individual presenter classes
    private final MainViewPresenter mainViewPresenter;
    private final SelectionViewPresenter selectionViewPresenter;
    private final VisualizationViewPresenter visualizationViewPresenter;

    private final CommandManager commandManager = new CommandManager();
    private final SelectionBinder binder;

    public GuiRegistry() throws IOException {
        URL mainFXML = getClass().getResource("/fxml/MainView.fxml");
        FXMLLoader loader = new FXMLLoader(mainFXML);
        root = loader.load();
        mainScene = new Scene(root);

        mainViewController = loader.getController();
        selectionViewController = mainViewController.getSelectionViewController();
        visualizationViewController = mainViewController.getVisualizationViewController();
        mainViewPresenter = new MainViewPresenter(this);
        visualizationViewPresenter = new VisualizationViewPresenter(this);
        selectionViewPresenter = new SelectionViewPresenter(this);

        binder = new SelectionBinder(visualizationViewPresenter.getHumanBody());
    }

    public Parent getRoot() {
        return root;
    }

    public Scene getMainScene() {
        return mainScene;
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

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public SelectionBinder getSelectionBinder() {
        return binder;
    }
}
