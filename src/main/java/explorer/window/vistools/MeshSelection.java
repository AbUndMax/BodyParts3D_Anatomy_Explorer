package explorer.window.vistools;

import explorer.model.AnatomyNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.*;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.shape.MeshView;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static javafx.collections.FXCollections.observableSet;

public class MeshSelection {

    // Observable list of currently selected Meshes -> SourceOfTruth FOR ALL SELECTIONS
    private final ObservableSet<MeshView> sourceOfTruth = observableSet();

    // connects fileID to a MeshView instance loaded from that fileID
    private final ConcurrentHashMap<String, MeshView> fileIdToMeshMap;

    // maintain mapping from TreeView -> TreeViewBinding
    private final Map<TreeView<AnatomyNode>, TreeViewBinding> treeViewBindings = new HashMap<>();

    /**
     * Constructs a MeshSelection object that manages bidirectional selection synchronization
     * between a HumanBody and multiple TreeViews.
     *
     * @param humanBody the HumanBody instance containing the MeshView mappings.
     */
    public MeshSelection(HumanBody humanBody) {
        fileIdToMeshMap = humanBody.getFileIdToMeshMap();
    }

    /**
     * Returns the observable set representing the source of truth for all MeshView selections.
     *
     * @return the observable set of currently selected MeshView objects.
     */
    public ObservableSet<MeshView> getSourceOfTruth() {
        return sourceOfTruth;
    }

    /**
     * Maps the provided file ID to the given MeshView object.
     *
     * @param fileId the identifier corresponding to the mesh file.
     * @param meshView the MeshView instance associated with the file ID.
     */
    protected void putFileIdToMesh(String fileId, MeshView meshView) {
        fileIdToMeshMap.put(fileId, meshView);
    }

    /**
     * Binds the given TreeView to the selection source of truth, ensuring bidirectional synchronization
     * between the TreeView selections and the selected MeshView objects.
     *
     * @param treeView the TreeView to bind to the selection model.
     */
    public void bindTreeView(TreeView<AnatomyNode> treeView) {

        TreeViewBinding binding = new TreeViewBinding(treeView);
        treeViewBindings.put(treeView, binding);

        MultipleSelectionModel<TreeItem<AnatomyNode>> multipleSelectionModel = treeView.getSelectionModel();
        BooleanProperty isSyncing = binding.isSyncing;

        // Push changes to the sourceOfTruth
        multipleSelectionModel.getSelectedItems().addListener((ListChangeListener<TreeItem<AnatomyNode>>) change -> {
            if (isSyncing.get()) return;
            isSyncing.set(true);

            while (change.next()) {
                if (change.wasAdded()) {
                    for (TreeItem<AnatomyNode> item : change.getAddedSubList()) {
                        LinkedList<String> fileIDs = item.getValue().getFileIDs();
                        if (fileIDs != null) {
                            for (String fileID : item.getValue().getFileIDs()) {
                                sourceOfTruth.add(fileIdToMeshMap.get(fileID));
                            }
                        }
                    }
                }
                if (change.wasRemoved()) {
                    for (TreeItem<AnatomyNode> item : change.getRemoved()) {
                        LinkedList<String> fileIDs = item.getValue().getFileIDs();
                        if (fileIDs != null) {
                            for (String fileID : item.getValue().getFileIDs()) {
                                sourceOfTruth.remove(fileIdToMeshMap.get(fileID));
                            }
                        }
                    }
                }
            }

            isSyncing.set(false);
        });

        // get changes from the SourceOfTruth
        sourceOfTruth.addListener((SetChangeListener<MeshView>) change -> {
            if (isSyncing.get()) return;
            isSyncing.set(true);

            if (change.wasAdded()) {
                selectNodeInTree(treeView, change.getElementAdded().getId());
            } else if (change.wasRemoved()) {
                deselectNodeInTree(treeView, change.getElementRemoved().getId());
            }

            isSyncing.set(false);
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

        TreeItem<AnatomyNode> itemToSelect = treeViewBindings.get(treeView).fileIdToNode.get(fileID);
        if (itemToSelect != null) {
            selectionModel.select(itemToSelect);
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

        TreeItem<AnatomyNode> itemToDeselect = treeViewBindings.get(treeView).fileIdToNode.get(fileID);
        if (itemToDeselect != null) {
            int index = treeView.getRow(itemToDeselect);
            selectionModel.clearSelection(index);
        }
    }

    private static class TreeViewBinding {
        TreeView<AnatomyNode> treeView;
        // map fileID to AnatomyNode
        Map<String, TreeItem<AnatomyNode>> fileIdToNode = new HashMap<>();
        BooleanProperty isSyncing = new SimpleBooleanProperty(false);

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
            LinkedList<String> fileIDs = current.getValue().getFileIDs();
            if (fileIDs != null) {
                for (String fileID : fileIDs){
                    fileIdToNode.put(fileID, current);
                }
            }
            for (TreeItem<AnatomyNode> child : current.getChildren()) {
                mapTree(child);
            }
        }
    }
}