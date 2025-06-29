package explorer.window.selection;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.*;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.shape.MeshView;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A MeshSelection is the SelectionModel for a HumanBody instance
 */
public class MultipleMeshSelectionModel extends MultipleSelectionModel<MeshView> {

    // holds full list of all meshes
    private final List<MeshView> allMeshes;

    // holds only selected mesh instances
    private final ObservableList<MeshView> selectedMeshes = FXCollections.observableArrayList();

    // mirrors selectedMeshes as Set, used to ensure a specific Mesh isnatce can only be selected once
    private final ObservableSet<MeshView> meshSet = FXCollections.observableSet();

    // holds the indices of selected Meshes (corresponding to the allMeshes List
    private final IntegerProperty currentSelectionIndex = new SimpleIntegerProperty(-1);

    public MultipleMeshSelectionModel(List<MeshView> allMeshes) {
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

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return FXCollections.unmodifiableObservableList(
            selectedMeshes.stream()
                .map(allMeshes::indexOf)
                .filter(idx -> idx >= 0)
                .collect(Collectors.toCollection(FXCollections::observableArrayList))
        );
    }

    @Override
    public ObservableList<MeshView> getSelectedItems() {
        return FXCollections.unmodifiableObservableList(selectedMeshes);
    }

    public IntegerProperty currentSelectionIndexProperty() {
        return currentSelectionIndex;
    }

    public int getCurrentSelectionIndex() {
        return currentSelectionIndex.get();
    }

    public void setCurrentSelectionIndex(int index) {
        this.currentSelectionIndex.set(index);
    }

    public boolean contains(MeshView mesh) {
        return meshSet.contains(mesh);
    }

    @Override
    public void select(MeshView meshView) {
        if (meshSet.add(meshView)) {
            selectedMeshes.add(meshView);
            int idx = allMeshes.indexOf(meshView);
            if (idx >= 0) {
                currentSelectionIndex.set(idx);
            }
        }
    }

    @Override
    public void clearSelection() {
        meshSet.clear();
        selectedMeshes.clear();
        currentSelectionIndex.set(-1);
    }

    @Override
    public void clearSelection(int index) {
        if (index >= 0 && index < allMeshes.size()) {
            MeshView meshView = allMeshes.get(index);
            if (meshSet.remove(meshView)) {
                selectedMeshes.remove(meshView);
                if (currentSelectionIndex.get() == index) {
                    currentSelectionIndex.set(-1);
                }
            }
        }
    }

    /**
     * Deselects the given MeshView instance, removing it from the selection.
     *
     * @param meshView The MeshView to deselect.
     */
    public void clearSelection(MeshView meshView) {
        if (meshView == null) return;
        int idx = allMeshes.indexOf(meshView);
        if (idx < 0) return;
        if (meshSet.remove(meshView)) {
            selectedMeshes.remove(meshView);
            if (currentSelectionIndex.get() == idx) {
                currentSelectionIndex.set(-1);
            }
        }
    }

    @Override
    public boolean isSelected(int index) {
        return index >= 0 && index < selectedMeshes.size();
    }

    public boolean isSelected(MeshView meshView) {
        if (meshView == null) return false;
        return meshSet.contains(meshView);
    }

    @Override
    public boolean isEmpty() {
        return selectedMeshes.isEmpty();
    }

    @Override
    public void selectIndices(int index, int... indices) {
        select(index);
        for (int idx : indices) {
            select(idx);
        }
    }

    @Override
    public void selectAll() {
        clearSelection();
        for (int i = 0; i < allMeshes.size(); i++) {
            select(i);
        }
    }

    @Override
    public void selectFirst() {
        if (!allMeshes.isEmpty()) {
            clearSelection();
            select(0);
        }
    }

    @Override
    public void selectLast() {
        if (!allMeshes.isEmpty()) {
            clearSelection();
            select(allMeshes.size() - 1);
        }
    }

    @Override
    public void clearAndSelect(int i) {
        clearSelection();
        select(i);
    }

    @Override
    public void select(int index) {
        if (index >= 0 && index < allMeshes.size()) {
            select(allMeshes.get(index));
        }
    }

    @Override
    public void selectPrevious() {
        if (allMeshes.isEmpty()) return;
        int idx = currentSelectionIndex.get();
        idx = (idx <= 0) ? allMeshes.size() - 1 : idx - 1;
        clearAndSelect(idx);
    }

    @Override
    public void selectNext() {
        if (allMeshes.isEmpty()) return;
        int idx = currentSelectionIndex.get();
        idx = (idx + 1) % allMeshes.size();
        clearAndSelect(idx);
    }

    public void traverseUnselectedMeshes(Consumer<MeshView> function) {
        for (MeshView mesh : allMeshes) {
            if (meshSet.contains(mesh)) continue;
            function.accept(mesh);
        }
    }
}