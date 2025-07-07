package explorer.window.command.commands;

import explorer.window.command.Command;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import java.util.Set;

/**
 * Command to remove a set of meshes from the anatomy group in the explorer window.
 * Provides undo functionality to restore the removed meshes.
 */
public class RemoveConceptCommand implements Command {

    private final Set<MeshView> meshesToRemove;
    private final Group anatomyGroup;

    /**
     * Constructs a RemoveConceptCommand for a given list of meshes and a target anatomy group.
     *
     * @param meshesToRemove the list of meshes to remove from the anatomy group
     * @param anatomyGroup the group from which the meshes will be removed
     */
    public RemoveConceptCommand(Set<MeshView> meshesToRemove, Group anatomyGroup) {
        this.meshesToRemove = meshesToRemove;
        this.anatomyGroup = anatomyGroup;
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Remove Concept";
    }

    /**
     * Executes the command by removing the specified meshes from the anatomy group.
     */
    @Override
    public void execute() {
        anatomyGroup.getChildren().removeAll(meshesToRemove);
    }

    /**
     * Undoes the remove operation by re-adding the previously removed meshes to the anatomy group.
     */
    @Override
    public void undo() {
        anatomyGroup.getChildren().addAll(meshesToRemove);
    }
}
