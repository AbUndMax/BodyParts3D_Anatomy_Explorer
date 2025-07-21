package explorer.window.presenter;

import explorer.model.Cladogram;
import explorer.model.treetools.ConceptNode;
import explorer.window.GuiRegistry;
import explorer.window.controller.ConceptInfoDialogController;
import explorer.window.vistools.DrawCladogram;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;

import java.util.Map;

/**
 * Presenter class for the NodeInfo view.
 * Manages user interactions and visual updates of the cladogram based on selected nodes.
 */
public class ConceptInfoDialogPresenter {

    private final ConceptInfoDialogController controller;

    /**
     * Creates a new presenter for the NodeInfo view.
     *
     * @param selectedItems the currently selected tree items
     * @param controller the controller managing the view
     * @param registry the global GUI registry (unused here, but available for future use)
     */
    public ConceptInfoDialogPresenter(ObservableList<TreeItem<ConceptNode>> selectedItems,
                                      ConceptInfoDialogController controller, GuiRegistry registry) {

        this.controller = controller;

        // get the selectedNode
        ConceptNode selectedNode = selectedItems.getFirst().getValue();

        // if there are multiple selected nodes,
        // setup a choiceBox that allows to switch between the multiple selected nodes
        if (selectedItems.size() > 1) {
            ChoiceBox<ConceptNode> nodeChoiceBox = controller.getNodeChoiceBox();
            setupNodeChoiceBox(nodeChoiceBox, selectedItems);
            selectedNode = nodeChoiceBox.getValue();
        }

        // draw the Tabs
        redrawCharacteristicsTab(selectedNode);
        redrawNodeDegDistTab(selectedNode);
        redrawTreeTab(selectedNode);
    }

    private void setupNodeChoiceBox(ChoiceBox<ConceptNode> nodeChoiceBox, ObservableList<TreeItem<ConceptNode>> selectedItems) {
        // Add all selected nodes to the choice box as selectable options
        for (TreeItem<ConceptNode> item : selectedItems) {
            nodeChoiceBox.getItems().add(item.getValue());
        }

        // set first selected Node by default
        nodeChoiceBox.setValue(nodeChoiceBox.getItems().getFirst());

        nodeChoiceBox.setOnAction(event -> {
            ConceptNode selectedNode = nodeChoiceBox.getValue();
            redrawCharacteristicsTab(selectedNode);
            redrawNodeDegDistTab(selectedNode);
            redrawTreeTab(selectedNode);
        });

        nodeChoiceBox.setVisible(true);
    }

    //TODO
    private void redrawCharacteristicsTab(ConceptNode selectedNode) {

    }

    //TODO
    private void redrawNodeDegDistTab(ConceptNode selectedNode) {

    }

    /**
     * Redraws the cladogram visualization for the given node.
     *
     * @param selectedNode the root node of the tree to display
     */
    private void redrawTreeTab(ConceptNode selectedNode) {
        StackPane treePane = controller.getTreePane();

        Map<ConceptNode, Point2D> map = Cladogram.layoutUniformEdgeLength(selectedNode);
        Group group = DrawCladogram.apply(selectedNode, map, treePane);

        treePane.getChildren().clear();
        treePane.getChildren().add(group);
    }
}
