package explorer.window.presenter;

import explorer.model.AnatomyNode;
import explorer.model.treetools.TreeUtils;
import explorer.model.treetools.KryoUtils;
import explorer.window.ControllerRegistry;
import explorer.window.controller.SelectionViewController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
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

        setupSearchBar(registry);
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

    private void setupSearchBar(ControllerRegistry registry) {
        TextField searchBar = registry.getVisualizationViewController().getTextFieldSearchBar();
        Button nextButton = registry.getVisualizationViewController().getButtonFindNext();
        Button firstButton = registry.getVisualizationViewController().getButtonFindFirst();
        Button allButton = registry.getVisualizationViewController().getButtonFindAll();
        Label hitLabel = registry.getVisualizationViewController().getSearchHitLabel();


        Search search = new Search();

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) search.resetSearch();
            else {
                search.performSearch(newValue, treeOfChoice(registry));
                if (search.getNumberOfHits() > 0) {
                    nextButton.setDisable(false);
                    firstButton.setDisable(false);
                    allButton.setDisable(false);
                } else {
                    nextButton.setDisable(true);
                    firstButton.setDisable(true);
                    allButton.setDisable(true);
                }
            }
        });

        nextButton.setOnAction(e -> {
            search.showNextResult(treeOfChoice(registry));
        });

        firstButton.setOnAction(e -> {
            search.showFirstResult(treeOfChoice(registry));
        });

        allButton.setOnAction(e -> {
            search.showAllResults(treeOfChoice(registry));
        });

        hitLabel.textProperty().bind(Bindings.createStringBinding(() ->
            (search.getCurrentSearchIndex() + 1) + " / " + search.getNumberOfHits() + " hits",
             search.currentSearchIndexProperty(), search.getSearchResults()
        ));
    }

    private TreeView<AnatomyNode> treeOfChoice(ControllerRegistry registry) {
        ChoiceBox<String> choiceBox = registry.getVisualizationViewController().getSearchChoice();

        TreeView<AnatomyNode> isATree = registry.getSelectionViewController().getTreeViewIsA();
        TreeView<AnatomyNode> partOfTree = registry.getSelectionViewController().getTreeViewPartOf();

        return choiceBox.getValue().equals("part-of") ? partOfTree : isATree;
    }

    private class Search {
        private final ObservableList<TreeItem<AnatomyNode>> searchResults = javafx.collections.FXCollections.observableArrayList();
        private final IntegerProperty currentSearchIndex = new SimpleIntegerProperty(-1);

        public ObservableList<TreeItem<AnatomyNode>> getSearchResults() {
            return searchResults;
        }

        public IntegerProperty currentSearchIndexProperty() {
            return currentSearchIndex;
        }

        public int getCurrentSearchIndex() {
            return currentSearchIndex.get();
        }

        public int getNumberOfHits() {
            return searchResults.size();
        }

        public void resetSearch() {
            currentSearchIndex.set(-1);
            searchResults.clear();
        }

        public void performSearch(String searchTerm, TreeView<AnatomyNode> treeView) {
            if (treeView == null || searchTerm.isEmpty()) return;

            TreeItem<AnatomyNode> root = treeView.getRoot();

            // reset search
            searchResults.clear();
            currentSearchIndex.set(-1);

            // collect Hits
            TreeUtils.preOrderTreeViewTraversal(root, item -> {
                if (item.getValue().getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    searchResults.add(item);
                }
            });

            if (!searchResults.isEmpty()) {
                currentSearchIndex.set(0);
                selectAndFocus(treeView, searchResults.get(currentSearchIndex.get()));
            }
        }

        public void showNextResult(TreeView<AnatomyNode> treeView) {
            if (searchResults.isEmpty()) return;

            currentSearchIndex.set((currentSearchIndex.get() + 1) % searchResults.size());
            selectAndFocus(treeView, searchResults.get(currentSearchIndex.get()));
        }

        public void showFirstResult(TreeView<AnatomyNode> treeView) {
            if (searchResults.isEmpty()) return;
            currentSearchIndex.set(0);
            selectAndFocus(treeView, searchResults.get(currentSearchIndex.get()));
        }

        public void showAllResults(TreeView<AnatomyNode> treeView) {
            if (searchResults.isEmpty()) return;

            MultipleSelectionModel<TreeItem<AnatomyNode>> selectionModel = treeView.getSelectionModel();
            selectionModel.clearSelection();

            for (TreeItem<AnatomyNode> item : searchResults) {
                selectionModel.select(item);
            }

            treeView.scrollTo(treeView.getRow(searchResults.get(0)));
        }

        private void selectAndFocus(TreeView<AnatomyNode> treeView, TreeItem<AnatomyNode> item) {
            treeView.getSelectionModel().clearSelection();
            treeView.getSelectionModel().select(item);
            treeView.scrollTo(treeView.getRow(item));
        }
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
