package explorer.window.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

import java.util.Arrays;

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
    private MenuItem menuButtonOpen;

    @FXML
    private MenuItem menuButtonExpandIsA;

    @FXML
    private MenuItem menuButtonExpandPartOf;

    @FXML
    private MenuItem menuButtonResetPosition;

    @FXML
    private MenuItem menuButtonResetSelection;

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

    public MenuItem getMenuButtonOpen() {
        return menuButtonOpen;
    }

    public MenuItem getMenuButtonExpandIsA() {
        return menuButtonExpandIsA;
    }

    public MenuItem getMenuButtonExpandPartOf() {
        return menuButtonExpandPartOf;
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

    /**
     * All controls set in the initializer are just for basic GUI behavior!
     * Nothing related to ANY model or window functionality!
     */
    @FXML
    public void initialize() {
        Platform.runLater(() -> mainSplitPane.setDividerPositions(0.4));
        fixDividerOnResize(mainSplitPane);
        setupAboutMessage();
        menuButtonClose.setOnAction(e -> Platform.exit());
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

    /**
     * sets up the "ABOUT" message of the Help-Menu
     */
    private void setupAboutMessage() {
        menuButtonAbout.setOnAction(e -> {
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
        });
    }
}
