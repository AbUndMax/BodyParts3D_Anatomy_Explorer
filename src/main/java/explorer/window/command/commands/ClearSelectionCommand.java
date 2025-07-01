package explorer.window.command.commands;

import explorer.model.AnatomyNode;
import explorer.window.command.Command;
import explorer.window.vistools.HumanBody;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.List;

public class ClearSelectionCommand implements Command {

    private final HumanBody humanBody;
    private final TreeView<AnatomyNode> treeViewIsA;
    private final TreeView<AnatomyNode> treeViewPartOf;
    private final TextField searchBar;

    private final List<MeshView> previousMeshSelection = new ArrayList<>();
    private final List<TreeItem<AnatomyNode>> previousIsASelection = new ArrayList<>();
    private final List<TreeItem<AnatomyNode>> previousPartOfSelection = new ArrayList<>();
    private String previousSearchText = "";

    public ClearSelectionCommand(HumanBody humanBody, TreeView<AnatomyNode> treeViewIsA, TreeView<AnatomyNode> treeViewPartOf, TextField searchBar) {
        this.humanBody = humanBody;
        previousMeshSelection.addAll(humanBody.getSelectionModel().getSelectedItems());
        this.treeViewIsA = treeViewIsA;
        previousIsASelection.addAll(treeViewIsA.getSelectionModel().getSelectedItems());
        this.treeViewPartOf = treeViewPartOf;
        previousPartOfSelection.addAll(treeViewPartOf.getSelectionModel().getSelectedItems());
        this.searchBar = searchBar;
        previousSearchText = searchBar.getText();
    }

    @Override
    public String name() {
        return "Clear Selection";
    }

    @Override
    public void execute() {
        humanBody.getSelectionModel().clearSelection();
        treeViewIsA.getSelectionModel().clearSelection();
        treeViewPartOf.getSelectionModel().clearSelection();
        searchBar.clear();
    }

    @Override
    public void undo() {
        for (MeshView mesh : previousMeshSelection) {
            humanBody.getSelectionModel().select(mesh);
        }

        for (TreeItem<AnatomyNode> item : previousIsASelection) {
            treeViewIsA.getSelectionModel().select(item);
        }

        for (TreeItem<AnatomyNode> item : previousPartOfSelection) {
            treeViewPartOf.getSelectionModel().select(item);
        }

        searchBar.setText(previousSearchText);
    }
}
