package explorer.model.treetools;

import javafx.scene.control.TreeItem;
import java.util.function.Consumer;


/**
 * Utility class providing common tree traversal and manipulation operations
 * for TreeItems containing AnatomyNode objects.
 * Includes methods for pre-order traversal, expanding, and collapsing tree nodes.
 */
public class TreeUtils {

    /**
     * Traverses the TreeView in pre-order starting from the given TreeItem.
     * Applies the provided function to each node during traversal.
     *
     * @param item the starting TreeItem for traversal
     * @param function a Consumer function to be applied to each visited TreeItem
     */
    public static void preOrderTreeViewTraversal(TreeItem<AnatomyNode> item, Consumer<TreeItem<AnatomyNode>> function) {
        function.accept(item);
        if (item != null) {
            for (TreeItem<AnatomyNode> child : item.getChildren()) {
                preOrderTreeViewTraversal(child, function);
            }
        }
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
