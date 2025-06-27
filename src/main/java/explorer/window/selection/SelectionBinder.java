package explorer.window.selection;

import explorer.model.AnatomyNode;
import explorer.window.vistools.HumanBody;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.shape.MeshView;
import java.util.*;
import java.util.concurrent.*;

/**
 * Binds a MeshSelection to TreeView and ListViews
  */
public class SelectionBinder {

    // Observable list of currently selected Meshes -> SourceOfTruth FOR ALL SELECTIONS
    MultipleMeshSelectionModel meshSelectionModel;

    // connects fileID to a MeshView instance loaded from that fileID
    private final ConcurrentHashMap<String, MeshView> fileIdToMeshMap;

    // maintain mapping from TreeView -> TreeViewBinding
    private final Map<TreeView<AnatomyNode>, TreeViewBinding> treeViewBindings = new HashMap<>();

    /**
     * Constructs a SlectionBinder object that manages bidirectional selection synchronization
     * between a HumanBody and multiple TreeViews.
     *
     * @param humanBody the HumanBody instance containing the MeshView mappings.
     */
    public SelectionBinder(HumanBody humanBody) {
        fileIdToMeshMap = humanBody.getFileIdToMeshMap();
        meshSelectionModel = humanBody.getSelectionModel();
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
                if (change.wasRemoved()) {
                    for (TreeItem<AnatomyNode> item : change.getRemoved()) {
                        LinkedList<String> fileIDs = item.getValue().getFileIDs();
                        if (fileIDs != null) {
                            for (String fileID : item.getValue().getFileIDs()) {
                                meshSelectionModel.clearSelection(fileIdToMeshMap.get(fileID));
                            }
                        }
                    }
                }
                if (change.wasAdded()) {
                    for (TreeItem<AnatomyNode> item : change.getAddedSubList()) {
                        LinkedList<String> fileIDs = item.getValue().getFileIDs();
                        //DEBUG
                        //System.out.println("processing:" + item.getValue().getName());
                        //System.out.println("fileIDs:" + fileIDs);
                        if (fileIDs != null) {
                            //System.out.println("fileID not null");
                            for (String fileID : item.getValue().getFileIDs()) {
                                //System.out.println("try to add:" + fileID);
                                meshSelectionModel.select(fileIdToMeshMap.get(fileID));

                                // Select all TreeItems associated with this fileID
                                Set<TreeItem<AnatomyNode>> associatedItems = treeViewBindings.get(treeView).fileIdToNode.get(fileID);
                                if (associatedItems != null) {
                                    for (TreeItem<AnatomyNode> associatedItem : associatedItems) {
                                        if (!multipleSelectionModel.getSelectedItems().contains(associatedItem)) {
                                            binding.scheduleSelection(() -> multipleSelectionModel.select(associatedItem));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            isSyncing.set(false);
        });

        // get changes from the SourceOfTruth
        meshSelectionModel.addListener(change -> {
            if (isSyncing.get()) return;
            isSyncing.set(true);

            while (change.next()) {
                if (change.wasAdded()) {
                    for (MeshView addedMesh : change.getAddedSubList()) {
                        TreeViewBinding b = treeViewBindings.get(treeView);
                        b.scheduleSelection(() -> selectNodeInTree(treeView, addedMesh.getId()));
                    }
                }
                if (change.wasRemoved()) {
                    for (MeshView removedMesh : change.getRemoved()) {
                        deselectNodeInTree(treeView, removedMesh.getId());
                    }
                }
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

        Set<TreeItem<AnatomyNode>> itemsToSelect = treeViewBindings.get(treeView).fileIdToNode.get(fileID);
        if (itemsToSelect != null) {
            TreeViewBinding binding = treeViewBindings.get(treeView);
            for (TreeItem<AnatomyNode> item : itemsToSelect) {
                binding.scheduleSelection(() -> selectionModel.select(item));
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

        Set<TreeItem<AnatomyNode>> itemsToDeSelect = treeViewBindings.get(treeView).fileIdToNode.get(fileID);
        if (itemsToDeSelect != null) {
            for (TreeItem<AnatomyNode> item : itemsToDeSelect) {
                int index = treeView.getRow(item);
                selectionModel.clearSelection(index);
            }
        }
    }

    public void bindListView(ListView<String> selectionList) {
        if (selectionList == null) return;

        // Listen to changes in the sourceOfTruth and update the ListView accordingly
        meshSelectionModel.addListener(change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (MeshView addedMesh : change.getAddedSubList()) {
                        //DEBUG
                        //System.out.println("added:" + addedMesh.getId());
                        @SuppressWarnings("unchecked")
                        HashSet<String> names = (HashSet<String>) addedMesh.getUserData();
                        for (String name : names) {
                            if (!selectionList.getItems().contains(name)) {
                                selectionList.getItems().add(name);
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
                            selectionList.getItems().remove(name);
                        }
                    }
                }
            }
        });

        // Disable interaction
        selectionList.setSelectionModel(null);
        selectionList.setFocusTraversable(false);
    }



    private static class TreeViewBinding {
        // Per-TreeView scheduler for debouncing selection updates
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private ScheduledFuture<?> pendingSelection;

        /**
         * Debounced execution of the given task on the JavaFX Application Thread.
         *
         * @param task the selection task to run.
         */
        void scheduleSelection(Runnable task) {
            if (pendingSelection != null && !pendingSelection.isDone()) {
                pendingSelection.cancel(false);
            }
            pendingSelection = scheduler.schedule(() -> Platform.runLater(task), 200, TimeUnit.MILLISECONDS);
        }

        TreeView<AnatomyNode> treeView;
        // map fileID to AnatomyNode -> Set of Nodes is used because one FileID can be associated with multiple concepts
        Map<String, Set<TreeItem<AnatomyNode>>> fileIdToNode = new HashMap<>();
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
                    Set<TreeItem<AnatomyNode>> set = fileIdToNode.getOrDefault(fileID, new HashSet<>());
                    set.add(current);
                    fileIdToNode.putIfAbsent(fileID, set);
                }
            }
            for (TreeItem<AnatomyNode> child : current.getChildren()) {
                mapTree(child);
            }
        }
    }
}
