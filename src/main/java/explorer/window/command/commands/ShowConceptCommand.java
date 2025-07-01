package explorer.window.command.commands;

import explorer.model.AnatomyNode;
import explorer.window.command.Command;
import explorer.window.vistools.HumanBody;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.shape.MeshView;

import java.util.LinkedList;

public class ShowConceptCommand implements Command {

    private final TreeView<AnatomyNode> lastTree;
    private final Group anatomyGroup;
    private final HumanBody humanBody;

    private final LinkedList<MeshView> initialShownMeshes = new LinkedList<>();
    private final LinkedList<MeshView> initialHiddenMeshes;

    public ShowConceptCommand(TreeView<AnatomyNode> lastTree, Group anatomyGroup, HumanBody humanBody) {
        this.lastTree = lastTree;
        this.anatomyGroup = anatomyGroup;
        this.humanBody = humanBody;

        for (var node : anatomyGroup.getChildren()) {
            if (node instanceof MeshView meshView) {
                initialShownMeshes.add(meshView);
            }
        }

        initialHiddenMeshes = new LinkedList<>(humanBody.getHiddenMeshes());
    }

    @Override
    public String name() {
        return "Show Concept";
    }

    @Override
    public void execute() {
        anatomyGroup.getChildren().clear();
        ObservableList<TreeItem<AnatomyNode>> selectedItems = lastTree.getSelectionModel().getSelectedItems();
        LinkedList<MeshView> meshesToDraw = new LinkedList<>();
        for (TreeItem<AnatomyNode> selectedItem : selectedItems) {
            for (String fileID : selectedItem.getValue().getFileIDs()) {
                meshesToDraw.add(humanBody.getMeshOfFileID(fileID));
            }
        }

        // add Meshes and reset hidden meshes
        anatomyGroup.getChildren().addAll(meshesToDraw);
        for (MeshView meshView : humanBody.getHiddenMeshes()) {
            meshView.setVisible(true);
        }
        humanBody.getHiddenMeshes().clear();
    }

    @Override
    public void undo() {
        // Restore initial visible meshes
        anatomyGroup.getChildren().clear();
        anatomyGroup.getChildren().addAll(initialShownMeshes);

        // Hide the meshes that were initially hidden
        for (MeshView mesh : initialHiddenMeshes) {
            mesh.setVisible(false);
        }

        // Restore the hidden meshes list
        humanBody.getHiddenMeshes().clear();
        humanBody.getHiddenMeshes().addAll(initialHiddenMeshes);
    }
}
