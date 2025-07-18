package explorer.model;

import explorer.model.treetools.ConceptNode;
import explorer.model.treetools.TreeUtils;
import javafx.geometry.Point2D;

import java.util.HashMap;
import java.util.Map;

/**
 * imported and modified from assignment04
 */
public class Cladogram {

    /**
     * Computes a layout for a tree structure such that all edges between nodes have the same length.
     * This method assigns each node a position in a 2D coordinate space based on its relationship
     * with its parent and siblings. The x-coordinates are incremented uniformly while y-coordinates
     * reflect subtree structure with nodes positioned based on averages or leaf counting.
     *
     * @param root the root node of the tree for which the layout is to be computed
     * @return a mapping of each node in the tree to its assigned position as a 2D point,
     *         represented by {@link Point2D}
     */
    public static Map<ConceptNode, Point2D> layoutUniformEdgeLength(ConceptNode root) {
        Map<ConceptNode, Point2D> result = new HashMap<>();

        // First postOrderTraversal for y coord calculation
        int[] leavesVisited = {0}; // Mutable counter using an array
        TreeUtils.postOrderTraversal(root, node -> {
            if (node.isLeaf()) {
                result.put(node, new Point2D(0, leavesVisited[0]));
                leavesVisited[0]++;
            } else {
                double y = computeYEqualLeafDepth(node, result);
                result.put(node, new Point2D(-1, y)); // x will be set in second pass
            }
        });

        // Second pass: set x-coordinates by depth
        setXCoordinatesByDepth(root, 0, result);

        return result;
    }

    /**
     * Assigns x-coordinates to each node in the tree based on their depth.
     * The deeper a node is in the tree, the greater its x-coordinate.
     * The y-coordinate remains unchanged.
     *
     * @param node the current node being processed
     * @param depth the depth of the current node (root starts at 0)
     * @param result the mapping from nodes to their current (x, y) positions
     */
    private static void setXCoordinatesByDepth(ConceptNode node, int depth, Map<ConceptNode, Point2D> result) {
        result.computeIfPresent(node, (k, oldPoint) -> new Point2D(depth, oldPoint.getY()));
        for (ConceptNode child : node.getChildren()) {
            setXCoordinatesByDepth(child, depth + 1, result);
        }
    }

    /**
     * Computes the average y-coordinate of all child nodes of the given node.
     * This assumes that the provided map contains the y-coordinates for all child nodes of the given node.
     *
     * @param node the node whose children's y-coordinates will be averaged
     * @param map a mapping of nodes to their respective points containing x and y coordinates
     * @return the average y-coordinate of the child nodes
     */
    public static double computeYEqualLeafDepth(ConceptNode node, Map<ConceptNode, Point2D> map) {
        double sum = 0;
        int counter = 0;
        for (ConceptNode child : node.getChildren()) {
            sum += map.get(child).getY();
            counter++;
        }
        return sum/counter;
    }
}
