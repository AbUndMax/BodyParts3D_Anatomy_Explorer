package explorer.window.presenter;

import explorer.model.AppConfig;
import explorer.window.GuiRegistry;
import explorer.window.controller.MainViewController;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class MainViewPresenter {

    private final MainViewController mainController;

    public MainViewPresenter(GuiRegistry registry) {
        mainController = registry.getMainViewController();
        setupMenuButtons(registry);
    }

    private void setupMenuButtons(GuiRegistry registry) {

        // EXPLORER
        /*
        instead of an "open" menuItem I decided to implement it in a different way for several reasons:
        - First: I designed the program to work exclusively on the initially loaded humanBody instance.
        - Second: It was not meant to load "new" or even different 3D objects since this would not match
                    the treeViews.
        - Third: I won't refactor this design choice since this would mean to manage the Listeners
                    on my HumanBody instance differently and would have to be rehooked onto the new instance
                    every time a new HumanBody is loaded.
         */
        mainController.getMenuButtonInvalidConfig().setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Config Path Invalidation");
            alert.setContentText("You are about to delete the config Path to the BodyParts3D wavefront folder.\n\n" +
                                         "This action will close the application.\n" +
                                         "On restart you are asked to set a new Path to the 'isa_BP3D_4.0_obj_99' folder.");

            ButtonType continueButton = new ButtonType("Continue");
            ButtonType abortButton = new ButtonType("Abort", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(continueButton, abortButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == continueButton) {
                AppConfig.invalidateLastPath();
                System.exit(0);
            }
        });

        // EDIT
        mainController.getMenuButtonResetSelection().setOnAction(event -> {
            registry.getSelectionViewController().getClearSelectionButton().fire();
        });

        // VIEW
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
        mainController.getMenuButtonShowSelectionList().setOnAction(event -> {
            registry.getSelectionViewController().getSelectionListToggle().fire();
        });

        // TRANSFORMATIONS
        mainController.getMenuButtonRotateUp().setOnAction(event -> {
            registry.getVisualizationViewPresenter().rotateContentGroupUp();
        });
        mainController.getMenuButtonRotateDown().setOnAction(event -> {
            registry.getVisualizationViewPresenter().rotateContentGroupDown();
        });
        mainController.getMenuButtonRotateLeft().setOnAction(event -> {
            registry.getVisualizationViewPresenter().rotateContentGroupLeft();
        });
        mainController.getMenuButtonRotateRight().setOnAction(event -> {
            registry.getVisualizationViewPresenter().rotateContentGroupRight();
        });

        mainController.getMenuButtonTranslateUp().setOnAction(event -> {
            registry.getVisualizationViewPresenter().translateContentGroupUp();
        });
        mainController.getMenuButtonTranslateDown().setOnAction(event -> {
            registry.getVisualizationViewPresenter().translateContentGroupDown();
        });
        mainController.getMenuButtonTranslateLeft().setOnAction(event -> {
            registry.getVisualizationViewPresenter().translateContentGroupLeft();
        });
        mainController.getMenuButtonTranslateRight().setOnAction(event -> {
            registry.getVisualizationViewPresenter().translateContentGroupRight();
        });

        mainController.getMenuButtonResetPosition().setOnAction(event -> {
            registry.getVisualizationViewPresenter().resetView();
        });
        mainController.getMenuButtonZoomIn().setOnAction(event -> {
            registry.getVisualizationViewPresenter().zoomIntoContentGroup();
        });
        mainController.getMenuButtonZoomOut().setOnAction(event -> {
            registry.getVisualizationViewPresenter().zoomOutContentGroup();
        });
    }
}
