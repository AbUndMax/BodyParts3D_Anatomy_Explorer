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
     * Returns the currently selected item from the last focused TreeView.
     *
     * @return The selected TreeItem in the last focused TreeView.
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

    /**
     * Initializes the provided TreeView with a tree structure loaded from a Kryo file.
     * Also sets the selection mode and tracks focus changes.
     *
     * @param treeView The TreeView to initialize.
     * @param kryoPath The path to the Kryo file containing the tree data.
     */
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
     * Recursively converts an AnatomyNode tree structure into TreeItems for display in a TreeView.
     *
     * @param treeRoot The root node of the AnatomyNode tree.
     * @return The corresponding TreeItem for the provided AnatomyNode.
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

    /**
     * Configures the selection, expansion, and collapse buttons and their associated actions.
     */
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

    /**
     * Sets up the search bar and its related buttons to perform search, navigate results,
     * and display hit counts within the TreeView.
     *
     * @param registry The ControllerRegistry containing references to UI elements.
     */
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

    /**
     * Returns the TreeView based on the user's choice in the ChoiceBox.
     *
     * @param registry The ControllerRegistry providing access to the TreeViews.
     * @return The selected TreeView (either isA or partOf).
     */
    private TreeView<AnatomyNode> treeOfChoice(ControllerRegistry registry) {
        ChoiceBox<String> choiceBox = registry.getVisualizationViewController().getSearchChoice();

        TreeView<AnatomyNode> isATree = registry.getSelectionViewController().getTreeViewIsA();
        TreeView<AnatomyNode> partOfTree = registry.getSelectionViewController().getTreeViewPartOf();

        return choiceBox.getValue().equals("part-of") ? partOfTree : isATree;
    }

    private class Search {
        /**
         * Returns the current list of search result TreeItems.
         *
         * @return The ObservableList of search result TreeItems.
         */
        public ObservableList<TreeItem<AnatomyNode>> getSearchResults() {
            return searchResults;
        }

        /**
         * Returns the IntegerProperty representing the current search index.
         *
         * @return The IntegerProperty for the current search index.
         */
        public IntegerProperty currentSearchIndexProperty() {
            return currentSearchIndex;
        }

        /**
         * Returns the current search index.
         *
         * @return The current index of the selected search result.
         */
        public int getCurrentSearchIndex() {
            return currentSearchIndex.get();
        }

        /**
         * Returns the number of search hits found.
         *
         * @return The number of search hits.
         */
        public int getNumberOfHits() {
            return searchResults.size();
        }

        /**
         * Resets the search by clearing all results and resetting the search index.
         */
        public void resetSearch() {
            currentSearchIndex.set(-1);
            searchResults.clear();
        }

        /**
         * Executes a search within the provided TreeView for nodes whose names contain the search term.
         * Updates the search results and focuses on the first match if found.
         *
         * @param searchTerm The search term to look for in node names.
         * @param treeView The TreeView to search within.
         */
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

        /**
         * Selects and focuses on the next search result in the TreeView, cycling through the results.
         *
         * @param treeView The TreeView where the search results are displayed.
         */
        public void showNextResult(TreeView<AnatomyNode> treeView) {
            if (searchResults.isEmpty()) return;

            currentSearchIndex.set((currentSearchIndex.get() + 1) % searchResults.size());
            selectAndFocus(treeView, searchResults.get(currentSearchIndex.get()));
        }

        /**
         * Selects and focuses on the first search result in the TreeView.
         *
         * @param treeView The TreeView where the search results are displayed.
         */
        public void showFirstResult(TreeView<AnatomyNode> treeView) {
            if (searchResults.isEmpty()) return;
            currentSearchIndex.set(0);
            selectAndFocus(treeView, searchResults.get(currentSearchIndex.get()));
        }

        /**
         * Selects all search results in the TreeView and scrolls to the first result.
         *
         * @param treeView The TreeView where the search results are displayed.
         */
        public void showAllResults(TreeView<AnatomyNode> treeView) {
            if (searchResults.isEmpty()) return;

            MultipleSelectionModel<TreeItem<AnatomyNode>> selectionModel = treeView.getSelectionModel();
            selectionModel.clearSelection();

            for (TreeItem<AnatomyNode> item : searchResults) {
                selectionModel.select(item);
            }

            treeView.scrollTo(treeView.getRow(searchResults.get(0)));
        }

        /**
         * Selects the provided TreeItem in the TreeView and scrolls to its position.
         *
         * @param treeView The TreeView containing the item.
         * @param item The TreeItem to select and focus.
         */
        private void selectAndFocus(TreeView<AnatomyNode> treeView, TreeItem<AnatomyNode> item) {
            treeView.getSelectionModel().clearSelection();
            treeView.getSelectionModel().select(item);
            treeView.scrollTo(treeView.getRow(item));
        }

        private final ObservableList<TreeItem<AnatomyNode>> searchResults = javafx.collections.FXCollections.observableArrayList();
        private final IntegerProperty currentSearchIndex = new SimpleIntegerProperty(-1);
    }

    /**
     * Recursively searches for a TreeItem with the specified concept ID within the given tree structure.
     *
     * @param current The current TreeItem being searched.
     * @param conceptID The concept ID to search for.
     * @return The matching TreeItem if found, otherwise null.
     */
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
