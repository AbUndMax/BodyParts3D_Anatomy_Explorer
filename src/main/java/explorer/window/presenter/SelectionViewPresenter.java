package explorer.window.presenter;

import explorer.model.AnatomyNode;
import explorer.model.SharedMultiSelectionModel;
import explorer.model.treeBuilder.KryoUtils;
import explorer.window.controller.SelectionViewController;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.collections.SetChangeListener;
import javafx.collections.ListChangeListener;

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

        setupSelectionModel(treeViewIsA, treeViewPartOf, selectionViewController.getSelectionListView());
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

    private void setupSelectionModel(TreeView<AnatomyNode> treeViewIsA, TreeView<AnatomyNode> treeViewPartOf, ListView<String> selectionList) {

        // sleection List is not interactable
        selectionList.setMouseTransparent(true);
        selectionList.setFocusTraversable(false);

        // multiple selection in each treeView allowed
        treeViewIsA.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeViewPartOf.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // instantiate a new SelectionModel
        SharedMultiSelectionModel sharedSelection = new SharedMultiSelectionModel();

        // bind the ListView to the live ObservableList of selected names
        selectionList.setItems(sharedSelection.getSelectedNames());

        // bind treeViewIsA to the selection model
        treeViewIsA.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<AnatomyNode>>) change -> {
            while (change.next()) {
                for (TreeItem<AnatomyNode> added : change.getAddedSubList()) {
                    sharedSelection.select(added.getValue());
                }
                for (TreeItem<AnatomyNode> removed : change.getRemoved()) {
                    sharedSelection.deselect(removed.getValue());
                }
            }
        });

        // bind treeViewPartOf to the selection model
        treeViewPartOf.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<AnatomyNode>>) change -> {
            while (change.next()) {
                for (TreeItem<AnatomyNode> added : change.getAddedSubList()) {
                    sharedSelection.select(added.getValue());
                }
                for (TreeItem<AnatomyNode> removed : change.getRemoved()) {
                    sharedSelection.deselect(removed.getValue());
                }
            }
        });

        // connect both treeViews to synchronize their selection
        sharedSelection.getSelectedConceptIDs().addListener((SetChangeListener<String>) change -> {
            if (change.wasAdded()) {
                TreeItem<AnatomyNode> matchA = findNodeByConceptID(treeViewIsA.getRoot(), change.getElementAdded());
                TreeItem<AnatomyNode> matchP = findNodeByConceptID(treeViewPartOf.getRoot(), change.getElementAdded());
                if (matchA != null) treeViewIsA.getSelectionModel().select(matchA);
                if (matchP != null) treeViewPartOf.getSelectionModel().select(matchP);
            }
            if (change.wasRemoved()) {
                TreeItem<AnatomyNode> matchA = findNodeByConceptID(treeViewIsA.getRoot(), change.getElementRemoved());
                TreeItem<AnatomyNode> matchP = findNodeByConceptID(treeViewPartOf.getRoot(), change.getElementRemoved());
                if (matchA != null) treeViewIsA.getSelectionModel().clearSelection(treeViewIsA.getRow(matchA));
                if (matchP != null) treeViewPartOf.getSelectionModel().clearSelection(treeViewPartOf.getRow(matchP));
            }
        });
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
