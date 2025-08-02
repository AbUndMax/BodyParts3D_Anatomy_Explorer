package explorer.window.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class MainViewController {

    // Root-Node for SelectionView.fxml
    @FXML
    private BorderPane selection;

    // Controller for SelectionView.fxml
    @FXML
    private SelectionViewController selectionController;

    // Root-Node for VisualizationView.fxml
    @FXML
    private BorderPane visualization;

    // Controller for VisualizationView.fxml
    @FXML
    private VisualizationViewController visualizationController;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private MenuItem menuButtonAbout;

    @FXML
    private MenuItem menuButtonClose;

    @FXML
    private MenuItem menuButtonExpandIsA;

    @FXML
    private MenuItem menuButtonCollapseIsA;

    @FXML
    private MenuItem menuButtonExpandPartOf;

    @FXML
    private MenuItem menuButtonCollapsePartOf;

    @FXML
    private MenuItem menuButtonExpandConcept;

    @FXML
    private MenuItem menuButtonCollapseConcept;

    @FXML
    private MenuItem menuButtonResetPosition;

    @FXML
    private MenuItem menuButtonResetSelection;

    @FXML
    private MenuItem menuButtonUndo;

    @FXML
    private MenuItem menuButtonRedo;

    @FXML
    private MenuItem menuButtonRotateDown;

    @FXML
    private MenuItem menuButtonRotateLeft;

    @FXML
    private MenuItem menuButtonRotateRight;

    @FXML
    private MenuItem menuButtonRotateUp;

    @FXML
    private MenuItem menuButtonTranslateDown;

    @FXML
    private MenuItem menuButtonTranslateLeft;

    @FXML
    private MenuItem menuButtonTranslateRight;

    @FXML
    private MenuItem menuButtonTranslateUp;

    @FXML
    private MenuItem menuButtonShowSelectionList;

    @FXML
    private MenuItem menuButtonZoomIn;

    @FXML
    private MenuItem menuButtonZoomOut;

    @FXML
    private MenuItem menuButtonInvalidConfig;

    @FXML
    private MenuItem fullScreenMenuItem;

    @FXML
    private RadioMenuItem lightModeMenuItem;

    @FXML
    private RadioMenuItem darkModeMenuItem;

    @FXML
    private MenuItem menuItemShowFindPane;

    @FXML
    private MenuItem nodeInformationsMenuItem;

    @FXML
    private MenuItem DebugWindowMenuItem;

    // Controller getters
    public SelectionViewController getSelectionViewController() {
        return selectionController;
    }

    public VisualizationViewController getVisualizationViewController() {
        return visualizationController;
    }

    public SplitPane getMainSplitPane() {
        return mainSplitPane;
    }

    public MenuItem getMenuButtonAbout() {
        return menuButtonAbout;
    }

    public MenuItem getMenuButtonClose() {
        return menuButtonClose;
    }

    public MenuItem getMenuButtonExpandIsA() {
        return menuButtonExpandIsA;
    }

    public MenuItem getMenuButtonCollapseIsA() {
        return menuButtonCollapseIsA;
    }

    public MenuItem getMenuButtonExpandPartOf() {
        return menuButtonExpandPartOf;
    }

    public MenuItem getMenuButtonCollapsePartOf() {
        return menuButtonCollapsePartOf;
    }

    public MenuItem getMenuButtonExpandConcept() {
        return menuButtonExpandConcept;
    }

    public MenuItem getMenuButtonCollapseConcept() {
        return menuButtonCollapseConcept;
    }

    public MenuItem getMenuButtonResetPosition() {
        return menuButtonResetPosition;
    }

    public MenuItem getMenuButtonResetSelection() {
        return menuButtonResetSelection;
    }

    public MenuItem getMenuButtonRotateDown() {
        return menuButtonRotateDown;
    }

    public MenuItem getMenuButtonRotateLeft() {
        return menuButtonRotateLeft;
    }

    public MenuItem getMenuButtonRotateRight() {
        return menuButtonRotateRight;
    }

    public MenuItem getMenuButtonRotateUp() {
        return menuButtonRotateUp;
    }

    public MenuItem getMenuButtonShowSelectionList() {
        return menuButtonShowSelectionList;
    }

    public MenuItem getMenuButtonZoomIn() {
        return menuButtonZoomIn;
    }

    public MenuItem getMenuButtonZoomOut() {
        return menuButtonZoomOut;
    }

    public MenuItem getMenuButtonTranslateDown() {
        return menuButtonTranslateDown;
    }

    public MenuItem getMenuButtonTranslateLeft() {
        return menuButtonTranslateLeft;
    }

    public MenuItem getMenuButtonTranslateRight() {
        return menuButtonTranslateRight;
    }

    public MenuItem getMenuButtonTranslateUp() {
        return menuButtonTranslateUp;
    }

    public MenuItem getMenuButtonInvalidConfig() {
        return menuButtonInvalidConfig;
    }

    public MenuItem getMenuButtonUndo() {
        return menuButtonUndo;
    }

    public MenuItem getMenuButtonRedo() {
        return menuButtonRedo;
    }

    public MenuItem getFullScreenMenuItem() {
        return fullScreenMenuItem;
    }

    public RadioMenuItem getLightModeMenuItem() {
        return lightModeMenuItem;
    }

    public RadioMenuItem getDarkModeMenuItem() {
        return darkModeMenuItem;
    }

    public MenuItem getMenuItemShowFindPane() {
        return menuItemShowFindPane;
    }

    public MenuItem getNodeInformationsMenuItem() {
        return nodeInformationsMenuItem;
    }

    public MenuItem getDebugWindowMenuItem() {
        return DebugWindowMenuItem;
    }

    /**
     * All controls set in the initializer are just for basic GUI behavior!
     * Nothing related to ANY model or window functionality!
     */
    @FXML
    public void initialize() {
        Platform.runLater(() -> mainSplitPane.setDividerPositions(0.4));
        fixDividerOnResize(mainSplitPane);
    }

    /**
     * Fixes a divider of a splitPane to a position when resizing the window.
     * @param splitPane
     */
    private static void fixDividerOnResize(SplitPane splitPane) {
        splitPane.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> {
                    // Speichere beim ersten Resize den aktuellen Pixelwert des Dividers
                    double fixedPixelPos = splitPane.getDividerPositions()[0] * oldWidth.doubleValue();
                    double newRelativePos = fixedPixelPos / newWidth.doubleValue();
                    splitPane.setDividerPositions(newRelativePos);
                });
            }
        });
    }
}
