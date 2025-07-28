package explorer.window.command.commands;

import explorer.model.treetools.ConceptNode;
import explorer.window.command.Command;
import explorer.window.vistools.HumanBodyMeshes;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.List;


/**
 * Command to clear all current selections in the explorer window, including mesh selections,
 * tree view selections, and the search bar content.
 * Provides undo functionality to restore the previous selections and search text.
 */
public class ClearSelectionCommand implements Command {

    private final HumanBodyMeshes humanBodyMeshes;
    private final TreeView<ConceptNode> treeViewIsA;
    private final TreeView<ConceptNode> treeViewPartOf;
    private final TextField searchBar;

    private final List<MeshView> previousMeshSelection = new ArrayList<>();
    private final List<TreeItem<ConceptNode>> previousIsASelection = new ArrayList<>();
    private final List<TreeItem<ConceptNode>> previousPartOfSelection = new ArrayList<>();
    private String previousSearchText = "";

    /**
     * Constructs a ClearSelectionCommand that captures the current selections and search text.
     *
     * @param humanBodyMeshes the HumanBody object managing mesh selections
     * @param treeViewIsA the tree view showing "is-a" relationships
     * @param treeViewPartOf the tree view showing "part-of" relationships
     * @param searchBar the search bar input field
     */
    public ClearSelectionCommand(HumanBodyMeshes humanBodyMeshes, TreeView<ConceptNode> treeViewIsA,
                                 TreeView<ConceptNode> treeViewPartOf, TextField searchBar) {

        this.humanBodyMeshes = humanBodyMeshes;
        previousMeshSelection.addAll(humanBodyMeshes.getSelectionModel().getListOfCurrentlySelectedItems());
        this.treeViewIsA = treeViewIsA;
        previousIsASelection.addAll(treeViewIsA.getSelectionModel().getSelectedItems());
        this.treeViewPartOf = treeViewPartOf;
        previousPartOfSelection.addAll(treeViewPartOf.getSelectionModel().getSelectedItems());
        this.searchBar = searchBar;
        previousSearchText = searchBar.getText();
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Clear Selection";
    }

    /**
     * Executes the command by clearing all mesh selections, tree view selections, and the search bar text.
     */
    @Override
    public void execute() {
        humanBodyMeshes.getSelectionModel().clearSelection();
        treeViewIsA.getSelectionModel().clearSelection();
        treeViewPartOf.getSelectionModel().clearSelection();
        searchBar.clear();
    }

    /**
     * Undoes the clear operation by restoring the previously captured mesh selections, tree view selections,
     * and search bar text.
     */
    @Override
    public void undo() {
        for (MeshView mesh : previousMeshSelection) {
            humanBodyMeshes.getSelectionModel().select(mesh);
        }

        for (TreeItem<ConceptNode> item : previousIsASelection) {
            treeViewIsA.getSelectionModel().select(item);
        }

        for (TreeItem<ConceptNode> item : previousPartOfSelection) {
            treeViewPartOf.getSelectionModel().select(item);
        }

        searchBar.setText(previousSearchText);
    }
}
