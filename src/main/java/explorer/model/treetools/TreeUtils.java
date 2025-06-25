package explorer.model.treetools;

import explorer.model.AnatomyNode;

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
}
