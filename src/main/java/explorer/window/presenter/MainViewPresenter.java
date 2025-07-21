package explorer.window.presenter;

import explorer.model.AppConfig;
import explorer.model.treetools.ConceptNode;
import explorer.window.GuiRegistry;
import explorer.window.command.Command;
import explorer.window.command.CommandManager;
import explorer.window.controller.MainViewController;
import explorer.window.controller.ConceptInfoDialogController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Presenter for the main application view, managing UI interactions,
 * command bindings, and application-level actions such as undo/redo and configuration management.
 */
public class MainViewPresenter {

    //Controller for accessing Nodes.
    private final MainViewController mainController;

    //Command manager for handling undo/redo logic.
    private final CommandManager commandManager;

    /**
     * Constructs a MainViewPresenter and initializes menu button handlers.
     *
     * @param registry the GuiRegistry providing access to controllers, command manager, and views
     */
    public MainViewPresenter(GuiRegistry registry) {
        mainController = registry.getMainViewController();
        commandManager = registry.getCommandManager();
        setupMenuButtons(registry);
    }

    /**
     * Configures all menu button actions in the main view, including:
     * - Explorer configuration invalidation
     * - Edit commands (undo/redo, reset selection)
     * - View controls (expand/collapse trees, toggle selection list)
     * - Transformation commands (rotate, translate, reset position, zoom)
     *
     * @param registry the GuiRegistry providing access to controllers and command manager
     */
    private void setupMenuButtons(GuiRegistry registry) {

        // EXPLORER
        mainController.getMenuButtonInvalidConfig().setOnAction(event -> {
            invalidateConfigHandler();
        });
        mainController.getMenuButtonClose().setOnAction(e -> {
            Platform.exit();
        });


        // EDIT
        mainController.getMenuButtonResetSelection().setOnAction(event -> {
            registry.getVisualizationViewController().getClearSelectionButton().fire();
        });
        setupUndoRedoItems(registry);


        // VIEW
        mainController.getFullScreenMenuItem().setOnAction(event -> {
            ((Stage) registry.getRoot().getScene().getWindow()).setFullScreen(true);
        });
        mainController.getLightModeMenuItem().setOnAction(event -> {
            registry.getMainScene().getStylesheets().clear();
            registry.getMainScene().getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource("/themes/lightMode.css")).toExternalForm());
        });
        mainController.getDarkModeMenuItem().setOnAction(event -> {
            registry.getMainScene().getStylesheets().clear();
            registry.getMainScene().getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource("/themes/darkMode.css")).toExternalForm());
        });
        mainController.getMenuButtonShowSelectionList().setOnAction(event -> {
            registry.getSelectionViewController().getSelectionListToggle().fire();
        });
        mainController.getMenuItemShowFindPane().setOnAction(event -> {
            registry.getSelectionViewController().getFindConceptsToggle().fire();
        });


        // TREEVIEW
        mainController.getMenuButtonExpandIsA().setOnAction(event -> {
            registry.getSelectionViewPresenter().expandIsATree();
        });
        mainController.getMenuButtonCollapseIsA().setOnAction(event -> {
            registry.getSelectionViewPresenter().collapseIsATree();
        });
        mainController.getMenuButtonExpandPartOf().setOnAction(event -> {
            registry.getSelectionViewPresenter().expandPartOfTree();
        });
        mainController.getMenuButtonCollapsePartOf().setOnAction(event -> {
            registry.getSelectionViewPresenter().collapsePartOfTree();
        });
        mainController.getNodeInformationsMenuItem().setOnAction(event -> {
            nodeInformationHandler(registry);
        });


        // TRANSFORMATIONS
        mainController.getMenuButtonRotateUp().setOnAction(event -> {
            registry.getVisualizationViewPresenter().rotateContentGroupUp(commandManager);
        });
        mainController.getMenuButtonRotateDown().setOnAction(event -> {
            registry.getVisualizationViewPresenter().rotateContentGroupDown(commandManager);
        });
        mainController.getMenuButtonRotateLeft().setOnAction(event -> {
            registry.getVisualizationViewPresenter().rotateContentGroupLeft(commandManager);
        });
        mainController.getMenuButtonRotateRight().setOnAction(event -> {
            registry.getVisualizationViewPresenter().rotateContentGroupRight(commandManager);
        });

        mainController.getMenuButtonTranslateUp().setOnAction(event -> {
            registry.getVisualizationViewPresenter().translateContentGroupUp(commandManager);
        });
        mainController.getMenuButtonTranslateDown().setOnAction(event -> {
            registry.getVisualizationViewPresenter().translateContentGroupDown(commandManager);
        });
        mainController.getMenuButtonTranslateLeft().setOnAction(event -> {
            registry.getVisualizationViewPresenter().translateContentGroupLeft(commandManager);
        });
        mainController.getMenuButtonTranslateRight().setOnAction(event -> {
            registry.getVisualizationViewPresenter().translateContentGroupRight(commandManager);
        });

        mainController.getMenuButtonResetPosition().setOnAction(event -> {
            registry.getVisualizationViewPresenter().resetView(commandManager);
        });
        mainController.getMenuButtonZoomIn().setOnAction(event -> {
            registry.getVisualizationViewPresenter().zoomIntoContentGroup(commandManager);
        });
        mainController.getMenuButtonZoomOut().setOnAction(event -> {
            registry.getVisualizationViewPresenter().zoomOutContentGroup(commandManager);
        });


        // HELP
        mainController.getMenuButtonAbout().setOnAction(e -> {
            aboutHandler();
        });
    }

    /**
     * Handles the action of invalidating the configuration path.
     * Shows a warning dialog and exits the application upon confirmation.
     */
    private void invalidateConfigHandler() {

        /*
        instead of an "open" menuItem I decided to implement it in a different way for several reasons:
        - First: I designed the program to work exclusively on the initially loaded humanBody instance.
        - Second: It was not meant to load "new" or even different 3D objects since this would not match
                    the treeViews.
        - Third: I won't refactor this design choice since this would mean to manage the Listeners
                    on my HumanBody instance differently and would have to be rehooked onto the new instance
                    every time a new HumanBody is loaded.
         */

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Config Path Invalidation");
        alert.setContentText("You are about to delete the config Path to the BodyParts3D wavefront folder.\n\n" +
                                     "This action will close the application.\n" +
                                     "On restart you are asked to set a new Path to the 'isa_BP3D_4.0_obj_99' " +
                                     "folder.");

        ButtonType continueButton = new ButtonType("Continue");
        ButtonType abortButton = new ButtonType("Abort", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(continueButton, abortButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == continueButton) {
            AppConfig.invalidateLastPath();
            Platform.exit();
        }
    }

    /**
     * Configures the Undo and Redo menu items including dynamic text binding
     * and enabling/disabling based on available commands.
     *
     * @param registry the GuiRegistry providing access to the command manager
     */
    private void setupUndoRedoItems(GuiRegistry registry) {

        MenuItem undoMenu = mainController.getMenuButtonUndo();
        MenuItem redoMenu = mainController.getMenuButtonRedo();

        // Bind Undo menu label to last undoable command
        undoMenu.textProperty().bind(Bindings.createStringBinding(()  -> {
            Command cmd = commandManager.getLastUndoCommand().getValue();
            return cmd != null ? "Undo: " + cmd.name() : "Undo";
        }, commandManager.getLastUndoCommand()));

        undoMenu.disableProperty().bind(commandManager.getLastUndoCommand().isNull());

        // Execute undo when Undo menu is selected
        undoMenu.setOnAction(event -> {
            commandManager.undo();
        });

        // Bind Redo menu label to last redoable command
        redoMenu.textProperty().bind(Bindings.createStringBinding(() -> {
            Command cmd = commandManager.getLastRedoCommand().getValue();
            return cmd != null ? "Redo: " + cmd.name() : "Redo";
        }, commandManager.getLastRedoCommand()));

        redoMenu.disableProperty().bind(commandManager.getLastRedoCommand().isNull());

        // Execute redo when Redo menu is selected
        redoMenu.setOnAction(event -> {
            commandManager.redo();
        });
    }

    /**
     * Opens a modal dialog displaying node information using the NodeInfoView.
     *
     * @param registry the GuiRegistry providing access to controllers and styles
     */
    private void nodeInformationHandler(GuiRegistry registry) {
        ObservableList<TreeItem<ConceptNode>> selectedItems =
                registry.getSelectionViewPresenter().getLastFocusedTreeView().getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            return;
        }

        try {
            TreeItem<ConceptNode> treeViewRoot = registry.getSelectionViewPresenter().getLastFocusedTreeView().getRoot();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConceptInfoDialog.fxml"));
            Parent root = loader.load();

            ConceptInfoDialogController conceptInfoController = loader.getController();

            new ConceptInfoDialogPresenter(selectedItems, treeViewRoot, conceptInfoController, registry);

            Stage infoStage = new Stage();
            conceptInfoController.getCloseNodeInfoButton().setOnAction(event -> infoStage.close());
            infoStage.setTitle("Concept Information");
            Scene scene = new Scene(root);
            if (mainController.getDarkModeMenuItem().isSelected()) {
                scene.getStylesheets().add(Objects.requireNonNull(
                        getClass().getResource("/themes/darkMode.css")).toExternalForm());
            }
            infoStage.setScene(scene);
            infoStage.initModality(Modality.APPLICATION_MODAL);
            infoStage.setMinWidth(600);
            infoStage.setMinHeight(420);
            infoStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an informational dialog with app credits and project details.
     */
    private void aboutHandler() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Anatomy Explorer    ᕙ(  •̀ ᗜ •́  )ᕗ");
        alert.setContentText("Implemented by Niklas Gerbes! \n" +
                                     "This app is the final project of the summer term course \n" +
                                     "\"Advanced Java for Bioinformatics\" \n" +
                                     "2025 U. Tübingen by Prof. D. Huson \n\n" +
                                     "The Explorer is based on \"BodyParts3D\":\n" +
                                     "Mitsuhashi N, Fujieda K, Tamura T, \n" +
                                     "Kawamoto S, Takagi T, Okubo K.\n" +
                                     "BodyParts3D: 3D structure database for anatomical concepts.\n" +
                                     "Nucleic Acids Res. 2008 Oct 3.\n" +
                                     "PMID: 18835852");
        alert.showAndWait();
    }
}
