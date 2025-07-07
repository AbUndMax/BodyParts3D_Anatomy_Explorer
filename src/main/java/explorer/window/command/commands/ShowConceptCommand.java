package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.HumanBodyMeshes;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Command to display a specific set of meshes in the explorer window.
 * Provides undo functionality to restore the previously visible and hidden meshes.
 */
public class ShowConceptCommand implements Command {

    private Set<MeshView> meshesToShow;
    private final Group anatomyGroup;
    private final HumanBodyMeshes humanBodyMeshes;
    private final boolean deleteExisting;

    private final ArrayList<MeshView> initialShownMeshes = new ArrayList<>();
    private final ArrayList<MeshView> initialHiddenMeshes;

    /**
     * Constructs a ShowConceptCommand with a list of meshes to show, the target anatomy group,
     * and the human body reference. Records the initial state of visible and hidden meshes.
     *
     * @param meshesToShow the meshes to be shown
     * @param anatomyGroup the group where the meshes will be displayed
     * @param humanBodyMeshes the human body model managing hidden meshes
     * @param deleteExisting flag indicating whether to clear existing meshes before showing the new ones
     */
    public ShowConceptCommand(Set<MeshView> meshesToShow, Group anatomyGroup, HumanBodyMeshes humanBodyMeshes, boolean deleteExisting) {
        this.meshesToShow = meshesToShow;
        this.anatomyGroup = anatomyGroup;
        this.humanBodyMeshes = humanBodyMeshes;
        this.deleteExisting = deleteExisting;

        for (var node : anatomyGroup.getChildren()) {
            if (node instanceof MeshView meshView) {
                initialShownMeshes.add(meshView);
            }
        }

        initialHiddenMeshes = new ArrayList<>(humanBodyMeshes.getHiddenMeshes());
    }

    @Override
    /**
     * @return the name of the command
     */
    public String name() {
        return "Show Concept";
    }

    @Override
    /**
     * Executes the command by showing the specified meshes.
     * Clears existing meshes if the deleteExisting flag is set.
     * Ensures no duplicate meshes are added and resets all hidden meshes to visible.
     */
    public void execute() {
        if (deleteExisting) {
            anatomyGroup.getChildren().clear();

        } else {
            // if the new meshes should be added, check such that meshes that are not already shown are added
            Set<MeshView> existingMeshes = new HashSet<>(initialShownMeshes);
            meshesToShow.removeAll(existingMeshes);
        }

        // add Meshes and reset hidden meshes
        anatomyGroup.getChildren().addAll(meshesToShow);
        for (MeshView meshView : humanBodyMeshes.getHiddenMeshes()) {
            meshView.setVisible(true);
        }
        humanBodyMeshes.getHiddenMeshes().clear();
    }

    @Override
    /**
     * Reverts the explorer window to its previous state by restoring the initially visible meshes
     * and hiding the meshes that were initially hidden.
     */
    public void undo() {
        // Restore initial visible meshes
        anatomyGroup.getChildren().clear();
        anatomyGroup.getChildren().addAll(initialShownMeshes);

        // Hide the meshes that were initially hidden
        for (MeshView mesh : initialHiddenMeshes) {
            mesh.setVisible(false);
        }

        // Restore the hidden meshes list
        humanBodyMeshes.getHiddenMeshes().clear();
        humanBodyMeshes.getHiddenMeshes().addAll(initialHiddenMeshes);
    }
}
