package explorer.window.presenter;

import explorer.model.AnatomyNode;
import explorer.model.treetools.TreeUtils;
import explorer.model.treetools.KryoUtils;
import explorer.window.GuiRegistry;
import explorer.window.controller.SelectionViewController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.TreeView;


/**
 * Presenter for the selection view, connecting TreeViews, search bar, and control buttons
 * with the anatomy model and selection logic.
 */
public class SelectionViewPresenter {

    private TreeView<AnatomyNode> lastFocusedTreeView = null;

    /**
     * @return The currently selected item from the last focused TreeView
     */
    private TreeItem<AnatomyNode> selectedItem() {
        return lastFocusedTreeView.getSelectionModel().getSelectedItem();
    }

    /**
     * @return the TreeView that most recently gained focus
     */
    public TreeView<AnatomyNode> getLastFocusedTreeView() {
        return lastFocusedTreeView;
    }

    private final SelectionViewController controller;

    /**
     * Constructs a SelectionViewPresenter, initializing TreeViews, buttons, and search bar for selection handling.
     *
     * @param registry the GuiRegistry providing access to controllers and UI components
     */
    public SelectionViewPresenter(GuiRegistry registry) {
        controller = registry.getSelectionViewController();

        TreeView<AnatomyNode> treeViewIsA = registry.getSelectionViewController().getTreeViewIsA();
        TreeView<AnatomyNode> treeViewPartOf = registry.getSelectionViewController().getTreeViewPartOf();

        setupTreeView(treeViewIsA, "src/main/resources/serializedTrees/isA_tree.kryo");
        setupTreeView(treeViewPartOf, "src/main/resources/serializedTrees/partOf_tree.kryo");

        // default is partOf tree
        lastFocusedTreeView = treeViewPartOf;

        setupButtons(registry);
        setupSelectionCounterLabels(registry);
        setupSearchBar(registry);
    }

    /**
     * Initializes the provided TreeView with a tree structure loaded from a Kryo file.
     * Also sets selection mode to multiple and updates focus to track the last focused TreeView.
     *
     * @param treeView the TreeView to initialize
     * @param kryoPath the path to the Kryo file containing the tree data
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
     * Configures the select, expand, and collapse buttons for the selection view.
     *
     * @param registry the GuiRegistry providing access to controllers and selection binder
     */
    private void setupButtons(GuiRegistry registry) {
        controller.getButtonSelectAtTreeNode().setOnAction(e -> {
            registry.getSelectionBinder().selectAllBelow(selectedItem(), lastFocusedTreeView);
        });

        controller.getExpandMenuItem().setOnAction(e -> TreeUtils.expandAllBelowGivenNode(selectedItem()));
        controller.getCollapseMenuItem().setOnAction(e -> TreeUtils.collapseAllNodesUptToGivenNode(selectedItem()));
    }

    /**
     * Sets up the search bar, find buttons, and hit count label for searching AnatomyNode names.
     *
     * @param registry the GuiRegistry providing access to selection view controls and TreeViews
     */
    private void setupSearchBar(GuiRegistry registry) {
        TextField searchBar = controller.getTextFieldSearchBar();
        Button nextButton = controller.getButtonFindNext();
        Button firstButton = controller.getButtonFindFirst();
        Button allButton = controller.getButtonFindAll();
        Label hitLabel = controller.getSearchHitLabel();


        Search search = new Search();

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                search.resetSearch();
                controller.getTreeViewIsA().getSelectionModel().clearSelection();
                controller.getTreeViewPartOf().getSelectionModel().clearSelection();
            }
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
     * Binds the Labels that show the number of selected Nodes / meshes to the respective TreeViews / selectionModels
     * @param registry
     */
    private void setupSelectionCounterLabels(GuiRegistry registry) {
        Label selectedNumberPartOf = controller.getNumberSelectedConceptsPartOfLabel();
        Label selectedNumberIsA = controller.getNumberSelectedConceptsIsALabel();
        Label selectedNumberMesh = controller.getNumberSelectedMeshesLabel();

        selectedNumberPartOf.textProperty().bind(
                Bindings.concat("part-of: ",
                                Bindings.size(
                                        controller.getTreeViewPartOf().getSelectionModel().getSelectedItems()
                                )
                )
        );

        selectedNumberIsA.textProperty().bind(
                Bindings.concat("is-a:    ",
                                Bindings.size(
                                        controller.getTreeViewIsA().getSelectionModel().getSelectedItems()
                                )
                )
        );

        selectedNumberMesh.textProperty().bind(
                Bindings.concat("model:   ",
                                Bindings.size(
                                        registry.getVisualizationViewPresenter()
                                                .getHumanBody().getSelectionModel().getSelectedItems()
                                ))
        );
    }

    /**
     * Returns the TreeView based on the user's choice in the search ChoiceBox.
     *
     * @param registry the GuiRegistry providing access to controllers
     * @return the selected TreeView (either isA or partOf)
     */
    private TreeView<AnatomyNode> treeOfChoice(GuiRegistry registry) {
        ChoiceBox<String> choiceBox = controller.getSearchChoice();

        TreeView<AnatomyNode> isATree = registry.getSelectionViewController().getTreeViewIsA();
        TreeView<AnatomyNode> partOfTree = registry.getSelectionViewController().getTreeViewPartOf();

        return choiceBox.getValue().equals("part-of") ? partOfTree : isATree;
    }

    /**
     * Encapsulates search state and functionality for TreeView items based on AnatomyNode names.
     * Maintains search results list and current index, and provides methods for performing and navigating search.
     */
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
         * Selects the provided TreeItem in the TreeView and scrolls it into view.
         *
         * @param treeView the TreeView containing the item
         * @param item the TreeItem to select and focus
         */
        private void selectAndFocus(TreeView<AnatomyNode> treeView, TreeItem<AnatomyNode> item) {
            treeView.getSelectionModel().clearSelection();
            treeView.getSelectionModel().select(item);
            treeView.scrollTo(treeView.getRow(item));
        }

        private final ObservableList<TreeItem<AnatomyNode>> searchResults =
                javafx.collections.FXCollections.observableArrayList();

        private final IntegerProperty currentSearchIndex = new SimpleIntegerProperty(-1);
    }

    /**
     * Expands all nodes in the 'is-a' TreeView.
     */
    public void expandIsATree() {
        TreeUtils.expandAllBelowGivenNode(controller.getTreeViewIsA().getRoot());
    }

    /**
     * Collapses all nodes in the 'is-a' TreeView up to the given node.
     */
    public void collapseIsATree() {
        TreeUtils.collapseAllNodesUptToGivenNode(controller.getTreeViewIsA().getRoot());
    }

    /**
     * Expands all nodes in the 'part-of' TreeView.
     */
    public void expandPartOfTree() {
        TreeUtils.expandAllBelowGivenNode(controller.getTreeViewPartOf().getRoot());
    }

    /**
     * Collapses all nodes in the 'part-of' TreeView up to the given node.
     */
    public void collapsePartOfTree() {
        TreeUtils.collapseAllNodesUptToGivenNode(controller.getTreeViewPartOf().getRoot());
    }
}
