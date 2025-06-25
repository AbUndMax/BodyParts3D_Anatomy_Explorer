package explorer.window.presenter;

import explorer.model.AnatomyNode;
import explorer.window.vistools.MeshSelection;
import explorer.model.treetools.KryoUtils;
import explorer.window.ControllerRegistry;
import explorer.window.controller.SelectionViewController;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;

public class SelectionViewPresenter {

    private TreeView<AnatomyNode> lastFocusedTreeView = null;

    public SelectionViewPresenter(ControllerRegistry registry) {
        SelectionViewController selectionViewController = registry.getSelectionViewController();
        setupTreeViews(registry);
        setupButtons(selectionViewController);
    }

    private void setupTreeViews(ControllerRegistry registry) {
        TreeView<AnatomyNode> treeViewIsA = registry.getSelectionViewController().getTreeViewIsA();
        AnatomyNode isATree = KryoUtils.loadTreeFromKryo("src/main/resources/serializedTrees/isA_tree.kryo");
        TreeItem<AnatomyNode> isATreeItemRoot = createTreeItemsRec(isATree);
        treeViewIsA.setRoot(isATreeItemRoot);
        treeViewIsA.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //TODO: connect TreeViews and HumanBody in selectionModel
        //TODO: Handle automatic expand for search -> expand all parents to the found entry


        TreeView<AnatomyNode> treeViewPartOf = registry.getSelectionViewController().getTreeViewPartOf();
        AnatomyNode partOfTree = KryoUtils.loadTreeFromKryo("src/main/resources/serializedTrees/partOf_tree.kryo");
        TreeItem<AnatomyNode> partOfTreeItemRoot = createTreeItemsRec(partOfTree);
        treeViewPartOf.setRoot(partOfTreeItemRoot);
        treeViewPartOf.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        setupSelectionModel(treeViewIsA, treeViewPartOf, registry.getSelectionViewController().getSelectionListView());
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

    private void setupButtons(SelectionViewController selectionViewController) {
        // selectionViewController.getButtonSelectAtTreeNode().setOnAction(e -> sharedMultiSelectionModel.select(selectedItem(selectAllBelowGivenNode(selectedItem());)));
        // TODO: setOnActions & eventually add new button: clear selection (austauschen von "inverse selection" to "clear selection")
        selectionViewController.getButtonExpandAtTreeNode().setOnAction(e ->
                expandAllBelowGivenNode(lastFocusedTreeView.getSelectionModel().getSelectedItem()));
        selectionViewController.getButtonCollapseAtTreeNode().setOnAction(e ->
                collapseAllNodesUptToGivenNode(lastFocusedTreeView.getSelectionModel().getSelectedItem()));
    }

    /**
     * This setups the synchronization between the selections!
     * The Method was generated with the help of AI: Simplification of code that I have written that worked but was hardly readable.
     * Comments by me!
     * @param treeViewIsA tree of isa-relation
     * @param treeViewPartOf tree of partOf-relation
     * @param selectionList selectionList that shows the current selection
     */
    private void setupSelectionModel(TreeView<AnatomyNode> treeViewIsA, TreeView<AnatomyNode> treeViewPartOf, ListView<String> selectionList) {

        treeViewIsA.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) lastFocusedTreeView = treeViewIsA;
        });

        treeViewPartOf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) lastFocusedTreeView = treeViewPartOf;
        });

        // sleection List is not interactable
        selectionList.setMouseTransparent(true);
        selectionList.setFocusTraversable(false);

        // multiple selection in each treeView allowed


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
    
    private void addToSelection(SelectionViewController selectionViewController) {
        // TODO: conrol selection only over selection button -> nur wenn leaf selected ist, entsprechende Meshes einfärben
    }

    /**
     * gets the selected Item from the tree that was last focused
     * @return
     */
    private TreeItem<AnatomyNode> selectedItem() {
        return lastFocusedTreeView.getSelectionModel().getSelectedItem();
    }

    /**
     * Recursively selects a given TreeItem and all its children in the provided selection model.
     *
     * @param item the TreeItem to start the selection process from, including its sub-items
     * @param selectionModel the MultipleSelectionModel used to manage selected TreeItems
     *
     * SOURCE: assignment03
     */
    private void selectAllBelowGivenNode(TreeItem<AnatomyNode> item, MultipleSelectionModel<TreeItem<AnatomyNode>> selectionModel) {
        selectionModel.select(item);
        for (TreeItem<AnatomyNode> child : item.getChildren()) {
            selectAllBelowGivenNode(child, selectionModel);
        }
    }

    /**
     * Expands the given TreeItem along with all of its child items recursively.
     * If the provided TreeItem is null, the method execution is skipped.
     *
     * @param item the TreeItem to be expanded, along with all its descendants
     */
    private void expandAllBelowGivenNode(TreeItem<AnatomyNode> item) {
        if (item != null) {
            item.setExpanded(true);
            for (TreeItem<AnatomyNode> child : item.getChildren()) {
                expandAllBelowGivenNode(child);
            }
        }
    }

    /**
     * Helper function to recursively collapse all nodes below the input node
     * @param item from which all nodes below get collapsed
     */
    private void collapseAllNodesUptToGivenNode(TreeItem<AnatomyNode> item) {
        if (item != null) {
            item.setExpanded(false);
            for (TreeItem<AnatomyNode> child : item.getChildren()) {
                collapseAllNodesUptToGivenNode(child);
            }
        }
    }
}
