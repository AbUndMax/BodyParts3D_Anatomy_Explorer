package explorer.window.selection;

import explorer.model.treetools.AnatomyNode;
import explorer.model.treetools.TreeUtils;
import explorer.window.vistools.HumanBodyMeshes;
import javafx.collections.ListChangeListener;
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
    private final Map<TreeView<AnatomyNode>, TreeViewBinding> treeViewBindings = new HashMap<>();

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
    public void bindTreeView(TreeView<AnatomyNode> treeView) {
        TreeViewBinding binding = new TreeViewBinding(treeView);
        treeViewBindings.put(treeView, binding);

        MultipleSelectionModel<TreeItem<AnatomyNode>> multipleSelectionModel = treeView.getSelectionModel();

        // Push changes to the sourceOfTruth
        multipleSelectionModel.getSelectedItems().addListener((ListChangeListener<TreeItem<AnatomyNode>>) change -> {
            if (binding.isSyncing) return;
            binding.isSyncing = true;

            List<MeshView> meshesToSelect = new ArrayList<>();
            List<MeshView> meshesToDeselect = new ArrayList<>();

            while (change.next()) {
                // Remove mesh selections when tree nodes are deselected
                if (change.wasRemoved()) {
                    for (TreeItem<AnatomyNode> item : change.getRemoved()) {
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
                    for (TreeItem<AnatomyNode> item : change.getAddedSubList()) {
                        ArrayList<String> fileIDs = item.getValue().getFileIDs();
                        //DEBUG
                        //System.out.println("processing:" + item.getValue().getName());
                        //System.out.println("fileIDs:" + fileIDs);

                        // same here: only leaves count as legitimate selection
                        if (fileIDs != null && item.getValue().isLeaf()) {
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
                        selectNodeInTree(treeView, addedMesh.getId());
                    }
                }
                if (change.wasRemoved()) {
                    for (MeshView removedMesh : change.getRemoved()) {
                        deselectNodeInTree(treeView, removedMesh.getId());
                    }
                }
            }
            binding.isSyncing = false;
        });
    }

    /**
     * Selects the TreeItem in the specified TreeView that corresponds to the provided file ID.
     *
     * @param treeView the TreeView containing the TreeItem to select.
     * @param fileID the file ID associated with the TreeItem to select.
     */
    private void selectNodeInTree(TreeView<AnatomyNode> treeView, String fileID) {
        MultipleSelectionModel<TreeItem<AnatomyNode>> selectionModel = treeView.getSelectionModel();
        TreeItem<AnatomyNode> root = treeView.getRoot();
        if (root == null) return;

        Set<TreeItem<AnatomyNode>> itemsToSelect = treeViewBindings.get(treeView).fileIdToTreeItem.get(fileID);
        if (itemsToSelect != null) {
            for (TreeItem<AnatomyNode> item : itemsToSelect) {
                // meshes are only represented DIRECTLY by leaves -> so only they get selected
                if (item.getValue().isLeaf()) selectionModel.select(item);
            }
        }
    }

    /**
     * Deselects the TreeItem in the specified TreeView that corresponds to the provided file ID.
     *
     * @param treeView the TreeView containing the TreeItem to deselect.
     * @param fileID the file ID associated with the TreeItem to deselect.
     */
    private void deselectNodeInTree(TreeView<AnatomyNode> treeView, String fileID) {
        MultipleSelectionModel<TreeItem<AnatomyNode>> selectionModel = treeView.getSelectionModel();
        TreeItem<AnatomyNode> root = treeView.getRoot();
        if (root == null) return;

        Set<TreeItem<AnatomyNode>> itemsToDeSelect = treeViewBindings.get(treeView).fileIdToTreeItem.get(fileID);
        if (itemsToDeSelect != null) {
            for (TreeItem<AnatomyNode> item : itemsToDeSelect) {
                int index = treeView.getRow(item);
                selectionModel.clearSelection(index);
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
    public void selectAllBelow(TreeItem<AnatomyNode> item, TreeView<AnatomyNode> treeView) {
        TreeViewBinding binding = treeViewBindings.get(treeView);

        // Temporarily disable sync to perform batch selection
        binding.isSyncing = true;
        ArrayList<MeshView> meshesToSelect = new ArrayList<>();
        MultipleSelectionModel<TreeItem<AnatomyNode>> selModel = treeView.getSelectionModel();
        selModel.clearSelection();

        // Traverse subtree to collect and select nodes and meshes
        TreeUtils.preOrderTreeViewTraversal(item, node -> {
            selModel.select(node);
            for (String fileID : node.getValue().getFileIDs()) {
                meshesToSelect.add(fileIdToMeshMap.get(fileID));
            }
        });

        // using Batch selection to fire only ONE event for the listeners -> Crucial for correct TreeView SelectionModel
        // selection above (receiving items from the source of truth)
        meshSelectionModel.selectAll(meshesToSelect);
        binding.isSyncing = false;
    }

    /**
     * Internal helper that maps file IDs to TreeItems for a specific TreeView.
     * Facilitates selection synchronization between mesh model and tree UI.
     */
    private static class TreeViewBinding {
        private final TreeView<AnatomyNode> treeView;
        // map fileID to TreeItem -> Set of Nodes is used because one FileID can be associated with multiple Items
        private final Map<String, Set<TreeItem<AnatomyNode>>> fileIdToTreeItem = new HashMap<>();
        private boolean isSyncing = false;

        /**
         * Constructs a TreeViewBinding for the given TreeView and maps its TreeItems by file ID.
         *
         * @param treeView the TreeView to bind and map.
         */
        TreeViewBinding(TreeView<AnatomyNode> treeView) {
            this.treeView = treeView;
            mapTree(treeView.getRoot());
        }

        /**
         * Recursively maps all TreeItems in the given TreeItem hierarchy by their associated file IDs.
         *
         * @param current the current TreeItem being mapped.
         */
        private void mapTree(TreeItem<AnatomyNode> current) {
            if (current == null) return;
            // Only map leaves so parents aren't selected for child fileIDs
            List<String> fileIDs = current.getValue().getFileIDs();
            if (fileIDs != null) {
                for (String fileID : fileIDs) {
                    Set<TreeItem<AnatomyNode>> set = fileIdToTreeItem
                        .computeIfAbsent(fileID, k -> new HashSet<>());
                    set.add(current);
                }
            }
            for (TreeItem<AnatomyNode> child : current.getChildren()) {
                mapTree(child);
            }
        }
    }
}
