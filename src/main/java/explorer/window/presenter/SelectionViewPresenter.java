package explorer.window.presenter;

import explorer.model.AnatomyNode;
import explorer.model.treetools.TreeUtils;
import explorer.model.treetools.KryoUtils;
import explorer.window.ControllerRegistry;
import explorer.window.controller.SelectionViewController;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;

public class SelectionViewPresenter {

    private TreeView<AnatomyNode> lastFocusedTreeView = null;

    /**
     * gets the selected Item from the tree that was last focused
     * @return
     */
    private TreeItem<AnatomyNode> selectedItem() {
        return lastFocusedTreeView.getSelectionModel().getSelectedItem();
    }

    private final SelectionViewController controller;

    public SelectionViewPresenter(ControllerRegistry registry) {
        controller = registry.getSelectionViewController();

        TreeView<AnatomyNode> treeViewIsA = registry.getSelectionViewController().getTreeViewIsA();
        TreeView<AnatomyNode> treeViewPartOf = registry.getSelectionViewController().getTreeViewPartOf();

        setupTreeView(treeViewIsA, "src/main/resources/serializedTrees/isA_tree.kryo");
        setupTreeView(treeViewPartOf, "src/main/resources/serializedTrees/partOf_tree.kryo");

        setupButtons();
    }

    private void setupTreeView(TreeView<AnatomyNode> treeView, String kryoPath) {
        AnatomyNode root = KryoUtils.loadTreeFromKryo(kryoPath);
        TreeItem<AnatomyNode> rootItem = createTreeItemsRec(root);
        treeView.setRoot(rootItem);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeView.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) lastFocusedTreeView = treeView;
        });
    }

    /**
     * Helper Function to recursively create TreeItems to populate the TreeView.
     *
     * SOURCE: assignment02
     */
    private static TreeItem<AnatomyNode> createTreeItemsRec(AnatomyNode treeRoot) {
        TreeItem<AnatomyNode> item = new TreeItem<>(treeRoot);
        if (!treeRoot.getChildren().isEmpty()) {
            for (AnatomyNode child : treeRoot.getChildren()) {
                item.getChildren().add(createTreeItemsRec(child));
            }
        }
        return item;
    }

    private void setupButtons() {
        controller.getButtonSelectAtTreeNode().setOnAction(e -> {
            TreeUtils.selectAllBelowGivenNode(selectedItem(), lastFocusedTreeView.getSelectionModel());
        });
        // TODO: setOnActions & eventually add new button: clear selection (austauschen von "inverse selection" to "clear selection")
        controller.getButtonExpandAtTreeNode().setOnAction(e ->
                TreeUtils.expandAllBelowGivenNode(selectedItem()));
        controller.getButtonCollapseAtTreeNode().setOnAction(e ->
                TreeUtils.collapseAllNodesUptToGivenNode(selectedItem()));
    }

    private TreeItem<AnatomyNode> findNodeByConceptID(TreeItem<AnatomyNode> current, String conceptID) {
        if (current.getValue().getConceptID().equals(conceptID)) {
            return current;
        }
        for (TreeItem<AnatomyNode> child : current.getChildren()) {
            TreeItem<AnatomyNode> hit = findNodeByConceptID(child, conceptID);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }
}
