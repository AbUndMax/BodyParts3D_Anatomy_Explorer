package explorer.window.vistools;

import explorer.model.AnatomyNode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.*;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.shape.MeshView;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static javafx.collections.FXCollections.observableSet;

/**
 * A MeshSelection is the SelectionModel for a HumanBody instance
 */
public class MeshSelection {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingSelection;

    // Observable list of currently selected Meshes -> SourceOfTruth FOR ALL SELECTIONS
    private final ObservableSet<MeshView> selectedMeshes = observableSet();

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

        // Initialize MouseClick Listener that registers if a mesh gets added to the selection or removed
        humanBody.setOnMouseClicked(event -> {
            Node clickedNode = event.getPickResult().getIntersectedNode();
            if (clickedNode instanceof MeshView meshView) {

                if (selectedMeshes.contains(meshView)) {
                    selectedMeshes.remove(meshView);
                } else {
                    selectedMeshes.add(meshView);
                }
            }
        });
    }

    public void activateDebug() {
        //DEBUG
        selectedMeshes.addListener((SetChangeListener<MeshView>) change -> {
            System.out.println("\n" + "----------".repeat(10));
            if (change.wasAdded()) System.out.println("added" + change.getElementAdded().getId());
            else if (change.wasRemoved()) System.out.println("removed" + change.getElementRemoved().getId());
            System.out.println("Current sourceOfTruth content:");
            selectedMeshes.forEach(mesh -> System.out.println(mesh.getId()));
            System.out.println("----------".repeat(10) + "\n");
        });
    }

    /**
     * Returns the observable set representing the source of truth for all MeshView selections.
     *
     * @return the observable set of currently selected MeshView objects.
     */
    public ObservableSet<MeshView> getSelectedMeshes() {
        return selectedMeshes;
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
                if (change.wasRemoved()) {
                    for (TreeItem<AnatomyNode> item : change.getRemoved()) {
                        LinkedList<String> fileIDs = item.getValue().getFileIDs();
                        if (fileIDs != null) {
                            for (String fileID : item.getValue().getFileIDs()) {
                                selectedMeshes.remove(fileIdToMeshMap.get(fileID));
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
                                selectedMeshes.add(fileIdToMeshMap.get(fileID));

                                // Select all TreeItems associated with this fileID
                                Set<TreeItem<AnatomyNode>> associatedItems = treeViewBindings.get(treeView).fileIdToNode.get(fileID);
                                if (associatedItems != null) {
                                    for (TreeItem<AnatomyNode> associatedItem : associatedItems) {
                                        if (!multipleSelectionModel.getSelectedItems().contains(associatedItem)) {
                                            scheduleSelection(() -> multipleSelectionModel.select(associatedItem));
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
        selectedMeshes.addListener((SetChangeListener<MeshView>) change -> {
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

        Set<TreeItem<AnatomyNode>> itemsToSelect = treeViewBindings.get(treeView).fileIdToNode.get(fileID);
        if (itemsToSelect != null) {
            for (TreeItem<AnatomyNode> item : itemsToSelect) {
                scheduleSelection(() -> selectionModel.select(item));
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
        selectedMeshes.addListener((SetChangeListener<MeshView>) change -> {
            //DEBUG
            //System.out.println("added:" + change.getElementAdded());
            //System.out.println("removed:" + change.getElementRemoved());
            if (change.wasAdded()) {
                @SuppressWarnings("unchecked")
                HashSet<String> names = (HashSet<String>) change.getElementAdded().getUserData();
                for (String name : names) {
                    if (!selectionList.getItems().contains(name)) {
                        selectionList.getItems().add(name);
                    }
                }
            } else if (change.wasRemoved()) {
                @SuppressWarnings("unchecked")
                HashSet<String> names = (HashSet<String>) change.getElementRemoved().getUserData();
                for (String name : names) {
                    selectionList.getItems().remove(name);
                }
            }
        });

        // Disable interaction
        selectionList.setSelectionModel(null);
        selectionList.setFocusTraversable(false);
    }

    public void scheduleSelection(Runnable selectionTask) {
        if (pendingSelection != null && !pendingSelection.isDone()) {
            pendingSelection.cancel(false);
        }
        pendingSelection = scheduler.schedule(() -> Platform.runLater(selectionTask), 200, TimeUnit.MILLISECONDS);
    }

    private static class TreeViewBinding {
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