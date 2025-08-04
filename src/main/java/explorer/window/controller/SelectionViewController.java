package explorer.window.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

public class SelectionViewController {
    @FXML
    private Button buttonCollapseAtTreeNode;

    @FXML
    private Button buttonDeselect;

    @FXML
    private Button buttonExpandAtTreeNode;

    @FXML
    private Button buttonInvertSelection;

    @FXML
    private Button buttonSelectAtTreeNode;

    @FXML
    private Button centerDivider1;

    @FXML
    private Button centerDivider2;

    @FXML
    private Button expandIsAView;

    @FXML
    private Button expandPartOfView;

    @FXML
    private RadioButton radioHideSelection;

    @FXML
    private RadioButton radioShowSelection;

    @FXML
    private BorderPane selectionListPane;

    @FXML
    private SplitPane selectionListSplitPane;

    @FXML
    private ToggleButton selectionListToggle;

    @FXML
    private ListView<?> selectionListView;

    @FXML
    private ToggleGroup selectionVisibility;

    @FXML
    private TreeView<?> treeViewIsA;

    @FXML
    private TreeView<?> treeViewPartOf;

    @FXML
    private SplitPane treeViewSplitPane;

    public Button getButtonCollapseAtTreeNode() {
        return buttonCollapseAtTreeNode;
    }

    public Button getButtonDeselect() {
        return buttonDeselect;
    }

    public Button getButtonExpandAtTreeNode() {
        return buttonExpandAtTreeNode;
    }

    public Button getButtonInvertSelection() {
        return buttonInvertSelection;
    }

    public Button getButtonSelectAtTreeNode() {
        return buttonSelectAtTreeNode;
    }

    public Button getCenterDivider1() {
        return centerDivider1;
    }

    public Button getCenterDivider2() {
        return centerDivider2;
    }

    public Button getExpandIsAView() {
        return expandIsAView;
    }

    public Button getExpandPartOfView() {
        return expandPartOfView;
    }

    public RadioButton getRadioHideSelection() {
        return radioHideSelection;
    }

    public RadioButton getRadioShowSelection() {
        return radioShowSelection;
    }

    public BorderPane getSelectionListPane() {
        return selectionListPane;
    }

    public SplitPane getSelectionListSplitPane() {
        return selectionListSplitPane;
    }

    public ToggleButton getSelectionListToggle() {
        return selectionListToggle;
    }

    public ListView<?> getSelectionListView() {
        return selectionListView;
    }

    public ToggleGroup getSelectionVisibility() {
        return selectionVisibility;
    }

    public TreeView<?> getTreeViewIsA() {
        return treeViewIsA;
    }

    public TreeView<?> getTreeViewPartOf() {
        return treeViewPartOf;
    }

    public SplitPane getTreeViewSplitPane() {
        return treeViewSplitPane;
    }

    /**
     * All controls set in the initializer are just for basic GUI behavior!
     * Nothing related to ANY model or window functionality!
     */
    @FXML
    public void initialize() {
        setDividerControls();
        setSelectionListControls();
    }

    /**
     * sets the corresponding actions for the divider buttons between the two treeViews
     * and ensures that the divider position
     * stays fully expanded to the set treeView on resize if it was before the resize
     */
    private void setDividerControls() {
        // those button control the position of the divider between the two treeViews
        centerDivider1.setOnAction(e -> treeViewSplitPane.setDividerPositions(0.5));
        centerDivider2.setOnAction(e -> treeViewSplitPane.setDividerPositions(0.5));

        expandIsAView.setOnAction(e -> treeViewSplitPane.setDividerPositions(0));
        expandPartOfView.setOnAction(e -> treeViewSplitPane.setDividerPositions(1));

        // treeView position is fixed to top or bottom if any treeView was fully expanded before!
        treeViewSplitPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.heightProperty().addListener((obsHeight, oldH, newH) -> {
                    double dividerPos = treeViewSplitPane.getDividerPositions()[0];

                    // if the splitView is fully expanded to any of the treeViews, the position is kept on resize!
                    if (dividerPos < 0.05) treeViewSplitPane.setDividerPositions(0);
                    else if (dividerPos > 0.95) treeViewSplitPane.setDividerPositions(1);
                });
            }
        });
    }

    /**
     * sets the correct toggle action for the selectionList panel
     */
    private void setSelectionListControls() {
        selectionListSplitPane.getItems().remove(selectionListPane);

        // listening to the toggle button
        selectionListToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            ObservableList<Node> items = selectionListSplitPane.getItems();

            if (isSelected && !items.contains(selectionListPane)) {
                // if button is selected, add the selectionPane and set the divider to the most right side
                items.add(selectionListPane);
                selectionListSplitPane.setDividerPositions(1);
            } else if (!isSelected && items.contains(selectionListPane)) {
                // if not selected, remove the selectionListPane
                items.remove(selectionListPane);
            }
        });
    }
}
