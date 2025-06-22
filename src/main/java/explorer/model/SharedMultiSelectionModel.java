package explorer.model;

import javafx.collections.*;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;

public class SharedMultiSelectionModel {

    private final ObservableSet<AnatomyNode> selectedAnatomyNodes = FXCollections.observableSet();
    private final ObservableSet<String> selectedConceptIDs = FXCollections.observableSet();
    private final ObservableList<String> selectedNames = FXCollections.observableArrayList();

    public SharedMultiSelectionModel() {
        selectedAnatomyNodes.addListener((SetChangeListener<AnatomyNode>) change -> {
            if (change.wasAdded()) {
                selectedNames.add(change.getElementAdded().getName());
                selectedConceptIDs.add(change.getElementAdded().getConceptID());
            }
            if (change.wasRemoved()) {
                selectedNames.remove(change.getElementRemoved().getName());
                selectedConceptIDs.remove(change.getElementRemoved().getConceptID());
            }
        });
    }

    public ObservableSet<String> getSelectedConceptIDs() {
        return selectedConceptIDs;
    }

    public ObservableList<String> getSelectedNames() {
        return selectedNames;
    }

    public void select(AnatomyNode node) {
        selectedAnatomyNodes.add(node);
    }

    public void deselect(AnatomyNode node) {
        selectedAnatomyNodes.remove(node);
    }

    public void clear() {
        selectedAnatomyNodes.clear();
    }

    public ObservableSet<AnatomyNode> getSelectedAnatomyNodes() {
        return selectedAnatomyNodes;
    }
}