package explorer.window.presenter;

import explorer.window.GuiRegistry;
import explorer.window.controller.MainViewController;

public class MainViewPresenter {

    private final MainViewController mainController;

    public MainViewPresenter(GuiRegistry registry) {
        mainController = registry.getMainViewController();
        setupMenuButtons(registry);
    }

    private void setupMenuButtons(GuiRegistry registry) {

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
