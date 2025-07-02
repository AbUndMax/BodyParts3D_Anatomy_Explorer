package explorer.window.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.DrawMode;

public class VisualizationViewController {
    @FXML
    private Button buttonCntrlDown;

    @FXML
    private Button buttonCntrlLeft;

    @FXML
    private Button buttonCntrlLeftDown;

    @FXML
    private Button buttonCntrlLeftUp;

    @FXML
    private Button buttonCntrlReset;

    @FXML
    private Button buttonCntrlRight;

    @FXML
    private Button buttonCntrlRightDown;

    @FXML
    private Button buttonCntrlRightUp;

    @FXML
    private Button buttonCntrlUp;

    @FXML
    private Button clearSelectionButton;

    @FXML
    private ToggleGroup drawMode;

    @FXML
    private ToggleButton hideModeToggle;

    @FXML
    private RadioButton radioFill;

    @FXML
    private RadioButton radioLines;

    @FXML
    private RadioButton radioRotation;

    @FXML
    private RadioButton radioTranslation;

    @FXML
    private Button redoButton;

    @FXML
    private Button resetHideButton;

    @FXML
    private ColorPicker selectionColorPicker;

    @FXML
    private ToggleGroup threeDControl;

    @FXML
    private Pane tripodPane;

    @FXML
    private Button undoButton;

    @FXML
    private Pane visualizationPane;

    @FXML
    private StackPane visualizationStackPane;

    @FXML
    private Slider zoomSlider;

    @FXML
    private SplitMenuButton showConceptButton;

    @FXML
    private MenuItem addToCurrentShowMenuItem;

    @FXML
    private MenuItem showFullHumanBodyMenuItem;

    public Button getButtonCntrlDown() {
        return buttonCntrlDown;
    }

    public Button getButtonCntrlLeft() {
        return buttonCntrlLeft;
    }

    public Button getButtonCntrlLeftDown() {
        return buttonCntrlLeftDown;
    }

    public Button getButtonCntrlLeftUp() {
        return buttonCntrlLeftUp;
    }

    public Button getButtonCntrlReset() {
        return buttonCntrlReset;
    }

    public Button getButtonCntrlRight() {
        return buttonCntrlRight;
    }

    public Button getButtonCntrlRightDown() {
        return buttonCntrlRightDown;
    }

    public Button getButtonCntrlRightUp() {
        return buttonCntrlRightUp;
    }

    public Button getButtonCntrlUp() {
        return buttonCntrlUp;
    }

    public Button getClearSelectionButton() {
        return clearSelectionButton;
    }

    public ToggleGroup getDrawMode() {
        return drawMode;
    }

    public ToggleButton getHideModeToggle() {
        return hideModeToggle;
    }

    public RadioButton getRadioFill() {
        return radioFill;
    }

    public RadioButton getRadioLines() {
        return radioLines;
    }

    public RadioButton getRadioRotation() {
        return radioRotation;
    }

    public RadioButton getRadioTranslation() {
        return radioTranslation;
    }

    public Button getRedoButton() {
        return redoButton;
    }

    public Button getResetHideButton() {
        return resetHideButton;
    }

    public ColorPicker getSelectionColorPicker() {
        return selectionColorPicker;
    }

    public ToggleGroup getThreeDControl() {
        return threeDControl;
    }

    public Pane getTripodPane() {
        return tripodPane;
    }

    public Button getUndoButton() {
        return undoButton;
    }

    public Pane getVisualizationPane() {
        return visualizationPane;
    }

    public StackPane getVisualizationStackPane() {
        return visualizationStackPane;
    }

    public Slider getZoomSlider() {
        return zoomSlider;
    }

    public SplitMenuButton getShowConceptButton() {
        return showConceptButton;
    }

    public MenuItem getAddToCurrentShowMenuItem() {
        return addToCurrentShowMenuItem;
    }

    public MenuItem getShowFullHumanBodyMenuItem() {
        return showFullHumanBodyMenuItem;
    }

    @FXML
    public void initialize() {
        radioLines.setUserData(DrawMode.LINE);
        radioFill.setUserData(DrawMode.FILL);
    }
}
