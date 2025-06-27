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
    private Button buttonFindAll;

    @FXML
    private Button buttonFindFirst;

    @FXML
    private Button buttonFindNext;

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
    private Button resetHideButton;

    @FXML
    private ChoiceBox<String> searchChoice;

    @FXML
    private Label searchHitLabel;

    @FXML
    private ColorPicker selectionColorPicker;

    @FXML
    private TextField textFieldSearchBar;

    @FXML
    private ToggleGroup threeDControl;

    @FXML
    private Pane tripodPane;

    @FXML
    private Pane visualizationPane;

    @FXML
    private StackPane visualizationStackPane;

    @FXML
    private Slider zoomSlider;

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

    public Button getButtonFindAll() {
        return buttonFindAll;
    }

    public Button getButtonFindFirst() {
        return buttonFindFirst;
    }

    public Button getButtonFindNext() {
        return buttonFindNext;
    }

    public RadioButton getRadioRotation() {
        return radioRotation;
    }

    public RadioButton getRadioTranslation() {
        return radioTranslation;
    }

    public Label getSearchHitLabel() {
        return searchHitLabel;
    }

    public TextField getTextFieldSearchBar() {
        return textFieldSearchBar;
    }

    public ToggleGroup getThreeDControl() {
        return threeDControl;
    }

    public StackPane getVisualizationStackPane() {
        return visualizationStackPane;
    }

    public Pane getTripodPane() {
        return tripodPane;
    }

    public Pane getVisualizationPane() {
        return visualizationPane;
    }

    public Slider getZoomSlider() {
        return zoomSlider;
    }

    public ColorPicker getSelectionColorPicker() {
        return selectionColorPicker;
    }

    public ToggleButton getHideModeToggle() {
        return hideModeToggle;
    }

    public Button getResetHideButton() {
        return resetHideButton;
    }

    public ChoiceBox<String> getSearchChoice() {
        return searchChoice;
    }

    public RadioButton getRadioFill() {
        return radioFill;
    }

    public RadioButton getRadioLines() {
        return radioLines;
    }

    public ToggleGroup getDrawMode() {
        return drawMode;
    }

    @FXML
    public void initialize() {
        searchChoice.setValue("part-of");
        radioLines.setUserData(DrawMode.LINE);
        radioFill.setUserData(DrawMode.FILL);
    }
}
