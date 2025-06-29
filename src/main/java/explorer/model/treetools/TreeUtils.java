package explorer.model.treetools;

import explorer.model.AnatomyNode;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import java.util.function.Consumer;

public class TreeUtils {

    /**
     * Traverses the tree in a post-order manner, starting from the given node and applies
     * the specified function to each node.
     *
     * @param node the root node of the subtree to be traversed in post-order
     * @param function a Consumer function to be applied to each node during traversal
     *
     * SOURCE: refactored after assignment03
     */
    public static void postOrderTraversal(AnatomyNode node, Consumer<AnatomyNode> function) {
        for (AnatomyNode child : node.getChildren()) {
            postOrderTraversal(child, function);
        }
        function.accept(node);
    }

    public static void preOrderTreeViewTraversal(TreeItem<AnatomyNode> item, Consumer<TreeItem<AnatomyNode>> function) {
        function.accept(item);
        for (TreeItem<AnatomyNode> child : item.getChildren()) {
            preOrderTreeViewTraversal(child, function);
        }
    }

    /**
     * Recursively selects a given TreeItem and all its children in the provided selection model.
     *
     * @param item the TreeItem to start the selection process from, including its sub-items
     * @param selectionModel the MultipleSelectionModel used to manage selected TreeItems
     */
    public static void selectAllBelowGivenNode(TreeItem<AnatomyNode> item, MultipleSelectionModel<TreeItem<AnatomyNode>> selectionModel) {
        preOrderTreeViewTraversal(item, selectionModel::select);
    }

    /**
     * Expands the given TreeItem along with all of its child items recursively.
     * If the provided TreeItem is null, the method execution is skipped.
     *
     * @param item the TreeItem to be expanded, along with all its descendants
     */
    public static void expandAllBelowGivenNode(TreeItem<AnatomyNode> item) {
        preOrderTreeViewTraversal(item, node -> {
            if (node != null) node.setExpanded(true);
        });
    }

    /**
     * Helper function to recursively collapse all nodes below the input node
     * @param item from which all nodes below get collapsed
     */
    public static void collapseAllNodesUptToGivenNode(TreeItem<AnatomyNode> item) {
        preOrderTreeViewTraversal(item, node -> {
            if (node != null) node.setExpanded(false);
        });
    }
}
