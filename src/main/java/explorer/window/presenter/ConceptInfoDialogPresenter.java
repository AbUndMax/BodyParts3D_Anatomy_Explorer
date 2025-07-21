package explorer.window.presenter;

import explorer.model.Cladogram;
import explorer.model.treetools.ConceptNode;
import explorer.model.treetools.TreeUtils;
import explorer.window.GuiRegistry;
import explorer.window.controller.ConceptInfoDialogController;
import explorer.window.vistools.DrawCladogram;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.util.Map;

/**
 * Presenter class for the NodeInfo view.
 * Manages user interactions and visual updates of the cladogram based on selected nodes.
 */
public class ConceptInfoDialogPresenter {

    private final GuiRegistry registry;
    private final ConceptInfoDialogController controller;
    private final TreeItem<ConceptNode> treeViewRoot;

    /**
     * Creates a new presenter for the NodeInfo view.
     *
     * @param selectedItems the currently selected tree items
     * @param controller the controller managing the view
     * @param registry the global GUI registry (unused here, but available for future use)
     */
    public ConceptInfoDialogPresenter(ObservableList<TreeItem<ConceptNode>> selectedItems,
                                      TreeItem<ConceptNode> treeViewRoot,
                                      ConceptInfoDialogController controller, GuiRegistry registry) {

        this.controller = controller;
        this.registry = registry;
        this.treeViewRoot = treeViewRoot;

        // get the selectedItem
        TreeItem<ConceptNode> selectedItem = selectedItems.getFirst();

        // if there are multiple selected nodes,
        // setup a choiceBox that allows to switch between the multiple selected nodes
        if (selectedItems.size() > 1) {
            ChoiceBox<TreeItem<ConceptNode>> nodeChoiceBox = controller.getNodeChoiceBox();
            setupNodeChoiceBox(nodeChoiceBox, selectedItems);
            selectedItem = nodeChoiceBox.getValue();
        }

        // draw the Tabs
        redrawCharacteristicsTab(selectedItem);
        redrawNodeDegDistTab(selectedItem);
        redrawTreeTab(selectedItem);
    }

    private void setupNodeChoiceBox(ChoiceBox<TreeItem<ConceptNode>> nodeChoiceBox, ObservableList<TreeItem<ConceptNode>> selectedItems) {
        // Add all selected nodes to the choice box as selectable options
        for (TreeItem<ConceptNode> item : selectedItems) {
            nodeChoiceBox.getItems().add(item);
        }

        // String presentation in choiceBox
        nodeChoiceBox.setConverter(new StringConverter<TreeItem<ConceptNode>>() {
            @Override
            public String toString(TreeItem<ConceptNode> conceptNodeTreeItem) {
                return conceptNodeTreeItem.getValue().getName();
            }

            @Override
            public TreeItem<ConceptNode> fromString(String s) {
                return null;
            }
        });

        // set first selected Node by default
        nodeChoiceBox.setValue(nodeChoiceBox.getItems().getFirst());

        nodeChoiceBox.setOnAction(event -> {
            TreeItem<ConceptNode> selectedItem = nodeChoiceBox.getValue();
            redrawCharacteristicsTab(selectedItem);
            redrawNodeDegDistTab(selectedItem);
            redrawTreeTab(selectedItem);
        });

        nodeChoiceBox.setVisible(true);
    }

    //TODO
    private void redrawCharacteristicsTab(TreeItem<ConceptNode> selectedItem) {
        ConceptNode selectedConcept = selectedItem.getValue();

        controller.getSelectedConceptLabel().setText(selectedConcept.getName());
        TreeItem<ConceptNode> parent = selectedItem.getParent();
        controller.getParentConceptLabel().setText(parent == null ? "No parent!" : parent.getValue().getName());

        controller.getDepthFromRootLabel().setText(String.valueOf(TreeUtils.calculateDepthToRoot(selectedItem)));
        controller.getNumberOfChildsLabel().setText(String.valueOf(selectedConcept.getChildren().size()));
        controller.getNumberOfSiblingsLabel().setText(String.valueOf(parent == null ? 0 : parent.getChildren().size()));
        controller.getNumberOfMeshesLabel().setText(String.valueOf(selectedConcept.getFileIDs().size()));

        controller.getSubtreeSizeLabel().setText(String.valueOf(TreeUtils.calculateTreeSize(selectedItem)));
        controller.getSubtreeHeightLabel().setText(String.valueOf(TreeUtils.horizontalTreeDepth(selectedConcept)));
        int leavesInSubtree = TreeUtils.numberOfLeaves(selectedConcept);
        controller.getNumberLeavesLabel().setText(String.valueOf(leavesInSubtree));
        double totalLeaves = TreeUtils.numberOfLeaves(treeViewRoot.getValue());
        double percentage = ((double) leavesInSubtree / totalLeaves) * 100;
        controller.getLeavesBelowLabel().setText(String.format(percentage == 100 ? "%.0f %%" : "%.2f %%", percentage));

    }

    //TODO
    private void redrawNodeDegDistTab(TreeItem<ConceptNode> selectedItem) {

    }

    /**
     * Redraws the cladogram visualization for the given node.
     *
     * @param selectedItem the root node of the tree to display
     */
    private void redrawTreeTab(TreeItem<ConceptNode> selectedItem) {
        ConceptNode selectedConcept = selectedItem.getValue();
        StackPane treePane = controller.getTreePane();

        Map<ConceptNode, Point2D> map = Cladogram.layoutUniformEdgeLength(selectedConcept);
        Group group = DrawCladogram.apply(selectedConcept, map, treePane);

        treePane.getChildren().clear();
        treePane.getChildren().add(group);
    }
}
