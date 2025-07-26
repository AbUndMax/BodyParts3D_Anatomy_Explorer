package explorer.model.treetools;

import javafx.scene.control.TreeItem;

import java.util.HashMap;
import java.util.Map;
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
    public static <T> void preOrderTreeViewTraversal(TreeItem<T> item, Consumer<TreeItem<T>> function) {
        function.accept(item);
        if (item != null) {
            for (TreeItem<T> child : item.getChildren()) {
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
    public static <T> void expandAllBelowGivenNode(TreeItem<T> item) {
        preOrderTreeViewTraversal(item, node -> {
            if (node != null) node.setExpanded(true);
        });
    }

    /**
     * Helper function to recursively collapse all nodes below the input node
     * @param item from which all nodes below get collapsed
     */
    public static <T> void collapseAllNodesUptToGivenNode(TreeItem<T> item) {
        preOrderTreeViewTraversal(item, node -> {
            if (node != null) node.setExpanded(false);
        });
    }

    /**
     * Traverses the tree in a post-order manner, starting from the given node and applies
     * the specified function to each node.
     *
     * imported and modified from assignment04
     *
     * @param node the root node of the subtree to be traversed in post-order
     * @param function a Consumer function to be applied to each node during traversal
     */
    public static void postOrderTraversal(ConceptNode node, Consumer<ConceptNode> function) {
        for (ConceptNode child : node.getChildren()) {
            postOrderTraversal(child, function);
        }
        function.accept(node);
    }

    /**
     * Computes and returns the number of leaf nodes in a tree starting from the current node.
     * A leaf node is defined as a node without any children.
     *
     * imported and modified from assignment04
     *
     * @return the total number of leaf nodes in the subtree rooted at the current node
     */
    public static int numberOfLeaves(ConceptNode conceptNode) {
        int[] numberOfLeaves = {0};
        TreeUtils.postOrderTraversal(conceptNode, node -> {
            if (node.isLeaf()) numberOfLeaves[0]++;
        });
        return numberOfLeaves[0];
    }

    /**
     * Calculates the maximum horizontal depth of the tree starting from the given node.
     * The horizontal depth is the number of internal nodes (with children)
     * along the longest path from this node to a leaf.
     *
     * imported and modified from assignment04
     *
     * @param conceptNode the starting node of the tree or subtree
     * @return the maximum horizontal depth
     */
    public static int horizontalTreeDepth(ConceptNode conceptNode) {
        if (conceptNode == null || conceptNode.getChildren().isEmpty()) {
            return 1;
        }

        int maxDepth = 0;
        for (ConceptNode child : conceptNode.getChildren()) {
            int childDepth = horizontalTreeDepth(child);
            if (childDepth > maxDepth) {
                maxDepth = childDepth;
            }
        }

        return 1 + maxDepth;
    }

    public static <T> int calculateDepthToRoot(TreeItem<T> treeItem) {
        return calculateDepthToRootRec(treeItem, 0);
    }

    private static <T> int calculateDepthToRootRec(TreeItem<T> treeItem, int currentDepth) {
        if (treeItem.getParent() == null) {
            return currentDepth;
        } else {
            return calculateDepthToRootRec(treeItem.getParent(), currentDepth + 1);
        }
    }

    public static <T> int calculateTreeSize(TreeItem<T> treeItem) {
        int[] currentSize = {0};
        preOrderTreeViewTraversal(treeItem, node -> currentSize[0]++);
        return currentSize[0];
    }

    public static Map<Integer, Integer> countNodesPerDepth(TreeItem<ConceptNode> root) {
        Map<Integer, Integer> depthCounts = new HashMap<>();
        traverseDepth(root, 0, depthCounts);
        return depthCounts;
    }

    private static void traverseDepth(TreeItem<ConceptNode> node, int depth, Map<Integer, Integer> depthCounts) {
        depthCounts.put(depth, depthCounts.getOrDefault(depth, 0) + 1);
        for (TreeItem<ConceptNode> child : node.getChildren()) {
            traverseDepth(child, depth + 1, depthCounts);
        }
    }

    public static <T> Map<Integer, Double> computeNormalizedNodeDegreeDistribution(TreeItem<T> root) {
        Map<Integer, Double> degreeCounts = new HashMap<>();
        int[] currentSize = {0};

        TreeUtils.preOrderTreeViewTraversal(root, node -> {
            int degree = node.getChildren().size();
            degreeCounts.put(degree, degreeCounts.getOrDefault(degree, 0.0) + 1);
            currentSize[0]++;
        });

        degreeCounts.replaceAll((d, v) -> degreeCounts.get(d) / currentSize[0]);

        return degreeCounts;
    }
}
