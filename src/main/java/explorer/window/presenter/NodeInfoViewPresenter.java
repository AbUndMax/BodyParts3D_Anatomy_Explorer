package explorer.window.presenter;

import explorer.model.Cladogram;
import explorer.model.treetools.AnatomyNode;
import explorer.window.GuiRegistry;
import explorer.window.controller.NodeInfoViewController;
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
public class NodeInfoViewPresenter {

    private final NodeInfoViewController controller;
    private final ObservableList<TreeItem<AnatomyNode>> selectedItems;

    /**
     * Creates a new presenter for the NodeInfo view.
     *
     * @param selectedItems the currently selected tree items
     * @param controller the controller managing the view
     * @param registry the global GUI registry (unused here, but available for future use)
     */
    public NodeInfoViewPresenter(ObservableList<TreeItem<AnatomyNode>> selectedItems, NodeInfoViewController controller, GuiRegistry registry) {
        this.controller = controller;
        this.selectedItems = selectedItems;

        setupTreeTap();
    }

    /**
     * Initializes the tree view based on the selected items.
     * If more than one item is selected, shows a ChoiceBox to switch between them.
     * Always renders the cladogram of the first selected node.
     */
    private void setupTreeTap() {
        if (selectedItems.size() != 1) {
            ChoiceBox<AnatomyNode> nodeChoiceBox = controller.getNodeChoiceBox();

            // Add all selected nodes to the choice box as selectable options
            for (TreeItem<AnatomyNode> item : selectedItems) {
                nodeChoiceBox.getItems().add(item.getValue());
            }

            nodeChoiceBox.setValue(nodeChoiceBox.getItems().getFirst());

            nodeChoiceBox.setOnAction(event -> {
                redrawTreePane(nodeChoiceBox.getSelectionModel().getSelectedItem());
            });

            nodeChoiceBox.setVisible(true);
        }

        redrawTreePane(selectedItems.getFirst().getValue());
    }

    /**
     * Redraws the cladogram visualization for the given node.
     *
     * @param selectedNode the root node of the tree to display
     */
    private void redrawTreePane(AnatomyNode selectedNode) {
        StackPane treePane = controller.getTreePane();

        Map<AnatomyNode, Point2D> map = Cladogram.layoutUniformEdgeLength(selectedNode);
        Group group = DrawCladogram.apply(selectedNode, map);

        treePane.getChildren().clear();
        treePane.getChildren().add(group);
    }
}
