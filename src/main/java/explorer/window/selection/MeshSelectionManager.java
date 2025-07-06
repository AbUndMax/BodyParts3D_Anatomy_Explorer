package explorer.window.selection;

import javafx.collections.*;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A MeshSelection is the SelectionModel for a HumanBody instance
 */
public class MeshSelectionManager {

    private final List<MeshView> allMeshes;

    // holds only selected mesh instances
    private final ObservableList<MeshView> selectedMeshes = FXCollections.observableArrayList();

    public MeshSelectionManager(List<MeshView> allMeshes) {
        this.allMeshes = allMeshes;
    }

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

    public void addListener(ListChangeListener<MeshView> listener) {
        selectedMeshes.addListener(listener);
    }

    public ObservableList<MeshView> getSelectedItems() {
        return FXCollections.unmodifiableObservableList(selectedMeshes);
    }

    public void select(MeshView meshView) {
        if (!selectedMeshes.contains(meshView)) {
            selectedMeshes.add(meshView);
        }
    }

    public void clearSelection() {
        selectedMeshes.clear();
    }

    /**
     * Deselects the given MeshView instance, removing it from the selection.
     *
     * @param meshView The MeshView to deselect.
     */
    public void clearSelection(MeshView meshView) {
        if (meshView == null) return;
        selectedMeshes.remove(meshView);
    }

    public boolean isSelected(MeshView meshView) {
        if (meshView == null) return false;
        return selectedMeshes.contains(meshView);
    }

    public boolean isEmpty() {
        return selectedMeshes.isEmpty();
    }

    public void selectAll() {
        List<MeshView> notSelectedYet = new ArrayList<>();
        for (MeshView mesh : allMeshes) {
            if (!selectedMeshes.contains(mesh)) {
                notSelectedYet.add(mesh);
            }
        }
        selectedMeshes.addAll(notSelectedYet);
    }

    public void selectAll(List<MeshView> meshViews) {
        if (meshViews == null) return;
        selectedMeshes.addAll(meshViews);
    }

    public void traverseUnselectedMeshes(Consumer<MeshView> function) {
        for (MeshView mesh : allMeshes) {
            if (selectedMeshes.contains(mesh)) continue;
            function.accept(mesh);
        }
    }
}