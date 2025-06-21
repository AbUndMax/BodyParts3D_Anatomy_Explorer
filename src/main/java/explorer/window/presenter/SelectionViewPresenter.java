package explorer.window.presenter;

import explorer.model.AnatomyNode;
import explorer.model.treeBuilder.KryoUtils;
import explorer.window.controller.SelectionViewController;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class SelectionViewPresenter {

    public SelectionViewPresenter(SelectionViewController selectionViewController) {
        setupTreeViews(selectionViewController);
    }

    private void setupTreeViews(SelectionViewController selectionViewController) {
        TreeView<AnatomyNode> treeViewIsA = selectionViewController.getTreeViewIsA();
        AnatomyNode isATree = KryoUtils.loadTreeFromKryo("src/main/resources/serializedTrees/isA_tree.kryo");
        TreeItem<AnatomyNode> isATreeItemRoot = createTreeItemsRec(isATree);
        treeViewIsA.setRoot(isATreeItemRoot);


        TreeView<AnatomyNode> treeViewPartOf = selectionViewController.getTreeViewPartOf();
        AnatomyNode partOfTree = KryoUtils.loadTreeFromKryo("src/main/resources/serializedTrees/partOf_tree.kryo");
        TreeItem<AnatomyNode> partOfTreeItemRoot = createTreeItemsRec(partOfTree);
        treeViewPartOf.setRoot(partOfTreeItemRoot);
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
}
