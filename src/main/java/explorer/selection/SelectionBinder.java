package explorer.selection;

import explorer.model.treetools.ConceptNode;
import explorer.model.treetools.TreeUtils;
import explorer.window.vistools.HumanBodyMeshes;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MeshView;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Binds MeshView selection state between a MeshSelection model and multiple TreeView and ListView controls.
 * Ensures bidirectional synchronization: tree and list UI components reflect the canonical mesh selection,
 * and user interactions in the UI update the selection model.
 */
public class SelectionBinder {

    // Observable list of currently selected Meshes -> SourceOfTruth FOR ALL SELECTIONS
    MeshSelectionManager meshSelectionModel;

    // connects fileID to a MeshView instance loaded from that fileID
    private final ConcurrentHashMap<String, MeshView> fileIdToMeshMap;

    // maintain mapping from TreeView -> TreeViewBinding
    private final Map<TreeView<ConceptNode>, TreeViewBinding> treeViewBindings = new HashMap<>();

    /**
     * Constructs a SelectionBinder that synchronizes mesh selections between the underlying
     * HumanBodyMeshes model and UI controls.
     *
     * @param humanBodyMeshes the HumanBodyMeshes providing mesh mappings and selection model
     */
    public SelectionBinder(HumanBodyMeshes humanBodyMeshes) {
        fileIdToMeshMap = humanBodyMeshes.getFileIdToMeshMap();
        meshSelectionModel = humanBodyMeshes.getSelectionModel();
    }

    /**
     * Binds a TreeView to the mesh selection model for bidirectional synchronization.
     * TreeItem selections update the mesh selection model, and mesh selection changes update the TreeView.
     *
     * @param treeView the TreeView displaying AnatomyNode items to bind.
     */
    public void bindTreeView(TreeView<ConceptNode> treeView) {
        TreeViewBinding binding = new TreeViewBinding(treeView);
        treeViewBindings.put(treeView, binding);
        mapConceptNamesIntoMeshUserData(treeView);

        MultipleSelectionModel<TreeItem<ConceptNode>> multipleSelectionModel = treeView.getSelectionModel();

        // Push changes to the sourceOfTruth
        multipleSelectionModel.getSelectedItems().addListener((ListChangeListener<TreeItem<ConceptNode>>) change -> {
            if (binding.isSyncing) return;
            binding.isSyncing = true;

            List<MeshView> meshesToSelect = new ArrayList<>();
            List<MeshView> meshesToDeselect = new ArrayList<>();

            while (change.next()) {
                // Remove mesh selections when tree nodes are deselected
                if (change.wasRemoved()) {
                    for (TreeItem<ConceptNode> item : change.getRemoved()) {
                        binding.selectionTracker.remove(item);
                        ArrayList<String> fileIDs = item.getValue().getFileIDs();

                        // collect the meshes that should be removed
                        if (fileIDs != null) {
                            for (String fileID : fileIDs) {
                                meshesToDeselect.add(fileIdToMeshMap.get(fileID));
                            }
                        }
                    }
                }
                // Add mesh selections when tree nodes are selected
                if (change.wasAdded()) {
                    for (TreeItem<ConceptNode> item : change.getAddedSubList()) {
                        ArrayList<String> fileIDs = item.getValue().getFileIDs();
                        //DEBUG
                        //System.out.println("processing:" + item.getValue().getName());
                        //System.out.println("fileIDs:" + fileIDs);

                        // same here: only leaves count as legitimate selection
                        if (fileIDs != null && item.getValue().isLeaf()) {
                            binding.selectionTracker.add(item);
                            //System.out.println("fileID not null");
                            for (String fileID : fileIDs) {
                                //System.out.println("try to add:" + fileID);
                                meshesToSelect.add(fileIdToMeshMap.get(fileID));
                            }
                        }
                    }
                }
            }


            // apply batch de-/selections
            meshSelectionModel.deselectAll(meshesToDeselect);
            meshSelectionModel.selectAll(meshesToSelect);

            binding.isSyncing = false;

        });

        // get changes from the SourceOfTruth
        meshSelectionModel.addListener(change -> {
            if (binding.isSyncing) return;
            binding.isSyncing = true;

            while (change.next()) {
                if (change.wasAdded()) {
                    for (MeshView addedMesh : change.getAddedSubList()) {
                        selectNodeInTree(binding, addedMesh.getId());
                    }
                }
                if (change.wasRemoved()) {
                    for (MeshView removedMesh : change.getRemoved()) {
                        deselectNodeInTree(binding, removedMesh.getId());
                    }
                }
            }

            cleanupTreeView(binding);

            binding.isSyncing = false;
        });
    }

    /**
     * Selects the TreeItem(s) in the bound TreeView that correspond to the provided file ID.
     * It uses the TreeViewBinding to perform the UI selection and update the internal selection tracker.
     *
     * @param binding the TreeViewBinding containing the target TreeView and selection tracker
     * @param fileID the file ID associated with the TreeItem(s) to select
     */
    private void selectNodeInTree(TreeViewBinding binding, String fileID) {
        TreeView<ConceptNode> treeView = binding.treeView;

        Set<TreeItem<ConceptNode>> itemsToSelect = treeViewBindings.get(treeView).fileIdToTreeItem.get(fileID);
        TreeItem<ConceptNode> lastLeaf = null;
        if (itemsToSelect != null) {
            for (TreeItem<ConceptNode> item : itemsToSelect) {
                // meshes are only represented DIRECTLY by leaves -> so only they get selected
                if (item.getValue().isLeaf()) {
                    binding.selectInBoundTree(item);
                    lastLeaf = item;
                }
            }
        }

        // scroll to the last selected item
        if (lastLeaf != null) {
            treeView.scrollTo(treeView.getRow(lastLeaf));
        }
    }

    /**
     * Deselects the TreeItem(s) in the bound TreeView that correspond to the provided file ID.
     * It uses the TreeViewBinding to clear the UI selection and update the internal selection tracker.
     *
     * @param binding the TreeViewBinding containing the target TreeView and selection tracker
     * @param fileID the file ID associated with the TreeItem(s) to deselect
     */
    private void deselectNodeInTree(TreeViewBinding binding, String fileID) {
        TreeView<ConceptNode> treeView = binding.treeView;
        TreeItem<ConceptNode> root = treeView.getRoot();
        if (root == null) return;

        Set<TreeItem<ConceptNode>> itemsToDeSelect = treeViewBindings.get(treeView).fileIdToTreeItem.get(fileID);
        if (itemsToDeSelect != null) {
            for (TreeItem<ConceptNode> item : itemsToDeSelect) {
                binding.clearInBoundTree(item);
            }
        }
    }

    /**
     * Synchronizes the TreeView selection with the canonical selection tracker.
     * Removes any 'ghost' selections (items selected in UI but not in tracker)
     * and re-applies any missing selections (items in tracker but not currently selected).
     *
     * @param binding the TreeViewBinding containing the target TreeView and selection tracker
     */
    private void cleanupTreeView(TreeViewBinding binding) {
        TreeView<ConceptNode> treeView = binding.treeView;


        Set<TreeItem<ConceptNode>> trueSelection = binding.selectionTracker;
        Set<TreeItem<ConceptNode>> selectedItems = new HashSet<>(treeView.getSelectionModel().getSelectedItems());

        Set<TreeItem<ConceptNode>> ghostSelection = new HashSet<>(selectedItems);
        ghostSelection.removeAll(trueSelection);

        Set<TreeItem<ConceptNode>> missingSelections = new HashSet<>(trueSelection);
        missingSelections.removeAll(selectedItems);

        // DEBUG
        // System.out.println("Binding Tracker: " + trueSelection);
        // System.out.println("Selected Items: " + selectedItems);
        // System.out.println("Ghost Selection: " + ghostSelection);
        // System.out.println("Missing Selection: " + missingSelections);

        // clearing selections that shouldn't have happened!
        for (TreeItem<ConceptNode> item : ghostSelection) {
            binding.clearInBoundTree(item);
        }

        // ensure that previous selection stays selected:
        // keeping track of previous selection and adding them back in resolved issue #35
        // it happened that the previous selection was dismissed (which seemed to happen when the tree was fully collapsed
        for (TreeItem<ConceptNode> item : missingSelections) {
            if (item.getValue().isLeaf()) {
                binding.selectInBoundTree(item);
            }
        }
    }

    /**
     * Binds a ListView of anatomy names to the mesh selection model.
     * Selected mesh names are shown in the list; list interactions are disabled.
     *
     * @param selectionList the ListView<String> to display selected anatomy names
     */
    public void bindListView(ListView<Label> selectionList, ColorPicker colorPicker) {
        if (selectionList == null) return;

        // Update ListView items when mesh selection model changes
        meshSelectionModel.addListener(change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (MeshView addedMesh : change.getAddedSubList()) {
                        //DEBUG
                        //System.out.println("added:" + addedMesh.getId());
                        @SuppressWarnings("unchecked")
                        HashSet<String> names = (HashSet<String>) addedMesh.getUserData();
                        for (String name : names) {
                            boolean alreadyExists = selectionList.getItems().stream()
                                    .anyMatch(label -> label.getText().equals(name));

                            Label label = new Label(name);

                            Circle colorCircle = new Circle(5);
                            colorCircle.setFill(colorPicker.getValue());
                            label.setGraphic(colorCircle);
                            label.setGraphicTextGap(8);

                            if (!alreadyExists) {
                                selectionList.getItems().add(label);

                            } else { // if it already exists, update its color to the new selected color
                                selectionList.getItems().removeIf(existingLabel -> existingLabel.getText().equals(name));
                                selectionList.getItems().add(label);
                            }
                        }
                    }
                } else if (change.wasRemoved()) {
                    for (MeshView removedMesh : change.getRemoved()) {
                        //DEBUG
                        //System.out.println("removed:" + removedMesh.getId());
                        @SuppressWarnings("unchecked")
                        HashSet<String> names = (HashSet<String>) removedMesh.getUserData();
                        for (String name : names) {
                            selectionList.getItems().removeIf(label -> label.getText().equals(name));
                        }
                    }
                }
            }
        });

        // Disable interaction
        selectionList.setSelectionModel(null);
        selectionList.setFocusTraversable(false);
    }

    /**
     * Selects all anatomy nodes and corresponding meshes under a given tree item.
     * Updates both TreeView selection and mesh selection model in batch.
     *
     * @param item the TreeItem subtree root to select
     * @param treeView the TreeView containing the item
     */
    public void selectAllBelow(TreeItem<ConceptNode> item, TreeView<ConceptNode> treeView) {
        if (item == null) return;
        TreeViewBinding binding = treeViewBindings.get(treeView);

        // Temporarily disable sync to perform batch selection
        binding.isSyncing = true;
        ArrayList<MeshView> meshesToSelect = new ArrayList<>();
        binding.clearSelection();

        // Traverse subtree to collect and select nodes and meshes
        TreeUtils.preOrderTreeViewTraversal(item, node -> {
            binding.selectInBoundTree(node);
            for (String fileID : node.getValue().getFileIDs()) {
                meshesToSelect.add(fileIdToMeshMap.get(fileID));
            }
        });

        cleanupTreeView(binding);

        // using Batch selection to fire only ONE event for the listeners -> Crucial for correct TreeView SelectionModel
        // selection above (receiving items from the source of truth)
        meshSelectionModel.selectAll(meshesToSelect);
        binding.isSyncing = false;
    }

    /**
     * Maps concept node names into the userData of their associated MeshView objects.
     *
     * This method traverses all leaf nodes in the provided TreeView. For each leaf node,
     * it retrieves the associated file IDs from the ConceptNode, then uses these IDs to
     * look up corresponding MeshView objects. If a MeshView already contains a HashSet of
     * concept names in its userData, the current ConceptNode's name is added to that set.
     *
     * I consider only leaves since I interprete Meshes as leaf concepts even though
     * internal Nodes may be associated with it.
     * But since I also only select Meshes if a leaf is selected in TreeView it is unnecessary
     * to list the internal names also.
     *
     * This allows the MeshView to store references to all anatomical concept names it represents.
     *
     * @param treeRoot the TreeView containing ConceptNode items whose names will be mapped
     *                 into the userData of their corresponding MeshView instances
     */
    private void mapConceptNamesIntoMeshUserData(TreeView<ConceptNode> treeRoot) {
        TreeUtils.preOrderTreeViewTraversal(treeRoot.getRoot(), node -> {
            if (node.isLeaf()) {
                ConceptNode conceptNode = node.getValue();
                for (String fileID : conceptNode.getFileIDs()) {
                    MeshView mesh = fileIdToMeshMap.get(fileID);
                    if (mesh.getUserData() instanceof HashSet<?> userData) {
                        @SuppressWarnings("unchecked") // not ideal, but since we won't reuse the userData it suffices
                        HashSet<String> conceptNames = (HashSet<String>) userData;
                        conceptNames.add(conceptNode.getName());
                    }
                }
            }
        });
    }

    /**
     * Internal helper that maps file IDs to TreeItems for a specific TreeView.
     * Facilitates selection synchronization between mesh model and tree UI.
     */
    private static class TreeViewBinding {
        private final TreeView<ConceptNode> treeView;
        private final ObservableSet<TreeItem<ConceptNode>> selectionTracker = FXCollections.observableSet(new HashSet<>());
        // map fileID to TreeItem -> Set of Nodes is used because one FileID can be associated with multiple Items
        private final Map<String, Set<TreeItem<ConceptNode>>> fileIdToTreeItem = new HashMap<>();
        private boolean isSyncing = false;

        /**
         * Constructs a TreeViewBinding for the given TreeView and maps its TreeItems by file ID.
         *
         * @param treeView the TreeView to bind and map.
         */
        TreeViewBinding(TreeView<ConceptNode> treeView) {
            this.treeView = treeView;
            mapTree(treeView.getRoot());
        }

        /**
         * Recursively maps all TreeItems in the given TreeItem hierarchy by their associated file IDs.
         *
         * @param current the current TreeItem being mapped.
         */
        private void mapTree(TreeItem<ConceptNode> current) {
            if (current == null) return;

            List<String> fileIDs = current.getValue().getFileIDs();
            if (fileIDs != null) {
                for (String fileID : fileIDs) {
                    Set<TreeItem<ConceptNode>> set = fileIdToTreeItem
                        .computeIfAbsent(fileID, k -> new HashSet<>());
                    set.add(current);
                }
            }
            for (TreeItem<ConceptNode> child : current.getChildren()) {
                mapTree(child);
            }
        }

        /**
         * Selects the given TreeItem in the bound TreeView and adds it to the selection tracker.
         *
         * @param item the TreeItem to select in the TreeView
         */
        private void selectInBoundTree(TreeItem<ConceptNode> item) {
            treeView.getSelectionModel().select(item);
            selectionTracker.add(item);
        }

        /**
         * Clears the selection of the given TreeItem in the bound TreeView and removes it from the selection tracker.
         *
         * @param item the TreeItem to deselect in the TreeView
         */
        private void clearInBoundTree(TreeItem<ConceptNode> item) {
            int index = treeView.getRow(item);
            treeView.getSelectionModel().clearSelection(index);
            selectionTracker.remove(item);
        }

        /**
         * Clears all selections in the bound TreeView and resets the internal selection tracker.
         */
        private void clearSelection() {
            treeView.getSelectionModel().clearSelection();
            selectionTracker.clear();
        }
    }
}
