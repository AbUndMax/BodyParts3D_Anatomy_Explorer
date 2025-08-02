package explorer.selection;

import javafx.collections.*;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages selection state for a collection of MeshView objects in a human body model.
 * Allows selecting, deselecting, and listening for selection changes.
 */
public class MeshSelectionManager {

    private final List<MeshView> allMeshes;

    // holds only selected mesh instances
    private final ObservableList<MeshView> selectedMeshes = FXCollections.observableArrayList();

    /**
     * Constructs a MeshSelectionManager for managing selection of the given list of meshes.
     *
     * @param allMeshes the complete list of MeshView objects available for selection
     */
    public MeshSelectionManager(List<MeshView> allMeshes) {
        this.allMeshes = allMeshes;
        activateDebug();
    }

    /**
     * Enables debug logging for selection changes, printing added and removed mesh IDs to the console.
     */
    public void activateDebug() {
        //DEBUG
        selectedMeshes.addListener((ListChangeListener<MeshView>) change -> {
            System.out.println("\n" + "----------".repeat(10));
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(mesh -> System.out.println("added: " + mesh.getId()));
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(mesh -> System.out.println("removed: " + mesh.getId()));
                }
            }
            System.out.println("Current sourceOfTruth content:");
            selectedMeshes.forEach(mesh -> System.out.println(mesh.getId()));
            System.out.println("----------".repeat(10) + "\n");
        });
    }

    public int getNumberOfSelectedMeshes() {
        return selectedMeshes.size();
    }

    /**
     * Adds a listener to observe changes in the selected mesh list.
     *
     * @param listener the ListChangeListener to add
     */
    public void addListener(ListChangeListener<MeshView> listener) {
        selectedMeshes.addListener(listener);
    }

    /**
     * Returns an unmodifiable list of currently selected MeshView instances.
     *
     * @return unmodifiable ObservableList of selected MeshView objects
     */
    public ObservableList<MeshView> getListOfCurrentlySelectedItems() {
        return FXCollections.unmodifiableObservableList(selectedMeshes);
    }

    /**
     * Selects the specified mesh if it is not already selected.
     *
     * @param meshView the MeshView to select
     */
    public void select(MeshView meshView) {
        if (!selectedMeshes.contains(meshView)) {
            selectedMeshes.add(meshView);
        }
    }

    /**
     * Clears all selected meshes.
     */
    public void clearSelection() {
        selectedMeshes.clear();
    }

    /**
     * Deselects the given mesh, removing it from the selection.
     *
     * @param meshView the MeshView to deselect
     */
    public void deselect(MeshView meshView) {
        if (meshView == null) return;
        selectedMeshes.remove(meshView);
    }

    public void deselectAll(List<MeshView> meshViews) {
        selectedMeshes.removeAll(meshViews);
    }

    /**
     * Checks if the given mesh is currently selected.
     *
     * @param meshView the MeshView to check
     * @return true if the mesh is selected, false otherwise
     */
    public boolean isSelected(MeshView meshView) {
        if (meshView == null) return false;
        return selectedMeshes.contains(meshView);
    }

    /**
     * Checks if there are no selected meshes.
     *
     * @return true if no meshes are selected, false otherwise
     */
    public boolean isEmpty() {
        return selectedMeshes.isEmpty();
    }

    /**
     * Selects all meshes from the complete mesh list.
     */
    public void selectAll() {
        List<MeshView> notSelectedYet = new ArrayList<>();
        for (MeshView mesh : allMeshes) {
            if (!selectedMeshes.contains(mesh)) {
                notSelectedYet.add(mesh);
            }
        }
        selectedMeshes.addAll(notSelectedYet);
    }

    /**
     * Selects all provided meshes, adding them to the current selection.
     *
     * @param meshViews the list of MeshView objects to select; if null, does nothing
     */
    public void selectAll(List<MeshView> meshViews) {
        if (meshViews == null) return;
        selectedMeshes.addAll(meshViews);
    }

    /**
     * Applies the provided function to each mesh that is not currently selected.
     *
     * @param function the Consumer to apply to each unselected mesh
     */
    public void traverseUnselectedMeshes(Consumer<MeshView> function) {
        for (MeshView mesh : allMeshes) {
            if (selectedMeshes.contains(mesh)) continue;
            function.accept(mesh);
        }
    }
}