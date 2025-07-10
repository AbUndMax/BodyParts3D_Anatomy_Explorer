package explorer.window.controller;

import explorer.model.AnatomyNode;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SelectionViewController {
    @FXML
    private MenuItem CollapseMenuItem;

    @FXML
    private ToggleButton findConceptsToggle;

    @FXML
    private VBox findBox;

    @FXML
    private Button buttonFindAll;

    @FXML
    private Button buttonFindFirst;

    @FXML
    private Button buttonFindNext;

    @FXML
    private Button buttonSelectAtTreeNode;

    @FXML
    private Button centerDivider1;

    @FXML
    private Button centerDivider2;

    @FXML
    private Button expandIsAView;

    @FXML
    private MenuItem expandMenuItem;

    @FXML
    private Button expandPartOfView;

    @FXML
    private ChoiceBox<String> searchChoice;

    @FXML
    private Label searchHitLabel;

    @FXML
    private BorderPane selectionListPane;

    @FXML
    private SplitPane selectionListSplitPane;

    @FXML
    private ToggleButton selectionListToggle;

    @FXML
    private ListView<Label> selectionListView;

    @FXML
    private TextField textFieldSearchBar;

    @FXML
    private MenuButton toggleMenuButton;

    @FXML
    private TreeView<AnatomyNode> treeViewIsA;

    @FXML
    private TreeView<AnatomyNode> treeViewPartOf;

    @FXML
    private SplitPane treeViewSplitPane;

    @FXML
    private Label numberSelectedConceptsPartOfLabel;

    @FXML
    private Label numberSelectedConceptsIsALabel;

    @FXML
    private Label numberSelectedMeshesLabel;

    public MenuItem getCollapseMenuItem() {
        return CollapseMenuItem;
    }

    public ToggleButton getFindConceptsToggle() {
        return findConceptsToggle;
    }

    public VBox getFindBox() {
        return findBox;
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

    public MenuItem getExpandMenuItem() {
        return expandMenuItem;
    }

    public Button getExpandPartOfView() {
        return expandPartOfView;
    }

    public ChoiceBox<String> getSearchChoice() {
        return searchChoice;
    }

    public Label getSearchHitLabel() {
        return searchHitLabel;
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

    public ListView<Label> getSelectionListView() {
        return selectionListView;
    }

    public TextField getTextFieldSearchBar() {
        return textFieldSearchBar;
    }

    public MenuButton getToggleMenuButton() {
        return toggleMenuButton;
    }

    public TreeView<AnatomyNode> getTreeViewIsA() {
        return treeViewIsA;
    }

    public TreeView<AnatomyNode> getTreeViewPartOf() {
        return treeViewPartOf;
    }

    public SplitPane getTreeViewSplitPane() {
        return treeViewSplitPane;
    }

    public Label getNumberSelectedConceptsPartOfLabel() {
        return numberSelectedConceptsPartOfLabel;
    }

    public Label getNumberSelectedConceptsIsALabel() {
        return numberSelectedConceptsIsALabel;
    }

    public Label getNumberSelectedMeshesLabel() {
        return numberSelectedMeshesLabel;
    }

    /**
     * All controls set in the initializer are just for basic GUI behavior!
     * Nothing related to ANY model or window functionality!
     */
    @FXML
    public void initialize() {
        setDividerControls();
        setSelectionListControls();
        setupSearchPane();
        searchChoice.setValue("part-of");
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

    private void setupSearchPane() {
        findBox.setVisible(false);
        findBox.setManaged(false);

        findConceptsToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            findBox.setVisible(isSelected);
            findBox.setManaged(isSelected);
        });
    }
}
