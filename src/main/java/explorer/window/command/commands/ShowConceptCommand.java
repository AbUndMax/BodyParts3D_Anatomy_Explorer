package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.HumanBody;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShowConceptCommand implements Command {

    private List<MeshView> meshesToShow;
    private final Group anatomyGroup;
    private final HumanBody humanBody;
    private final boolean deleteExisting;

    private final ArrayList<MeshView> initialShownMeshes = new ArrayList<>();
    private final ArrayList<MeshView> initialHiddenMeshes;

    public ShowConceptCommand(List<MeshView> meshesToShow, Group anatomyGroup, HumanBody humanBody, boolean deleteExisting) {
        this.meshesToShow = meshesToShow;
        this.anatomyGroup = anatomyGroup;
        this.humanBody = humanBody;
        this.deleteExisting = deleteExisting;

        for (var node : anatomyGroup.getChildren()) {
            if (node instanceof MeshView meshView) {
                initialShownMeshes.add(meshView);
            }
        }

        initialHiddenMeshes = new ArrayList<>(humanBody.getHiddenMeshes());
    }

    @Override
    public String name() {
        return "Show Concept";
    }

    @Override
    public void execute() {
        if (deleteExisting) {
            anatomyGroup.getChildren().clear();

        } else {
            // if the new meshes should be added, check such that meshes that are not already shown are added
            Set<MeshView> existingMeshes = new HashSet<>(initialShownMeshes);
            Set<MeshView> meshesToAdd = new HashSet<>(meshesToShow);

            meshesToAdd.removeAll(existingMeshes);

            meshesToShow = new ArrayList<>(meshesToAdd);
        }

        // add Meshes and reset hidden meshes
        anatomyGroup.getChildren().addAll(meshesToShow);
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
