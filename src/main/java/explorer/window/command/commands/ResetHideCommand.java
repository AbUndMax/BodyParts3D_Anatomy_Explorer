package explorer.window.command.commands;

import explorer.window.command.Command;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;


/**
 * Command to reset the visibility of all hidden meshes in the explorer window.
 * Provides undo functionality to re-hide the previously hidden meshes.
 */
public class ResetHideCommand implements Command {

    private final ArrayList<MeshView> hiddenMeshes;
    private final ArrayList<MeshView> backupMeshes;

    /**
     * Constructs a ResetHideCommand with the current list of hidden meshes.
     * Creates a backup of the hidden meshes for undo functionality.
     *
     * @param hiddenMeshes the list of currently hidden meshes
     */
    public ResetHideCommand(ArrayList<MeshView> hiddenMeshes) {
        this.hiddenMeshes = hiddenMeshes;
        this.backupMeshes = new ArrayList<>(hiddenMeshes);
    }

    @Override
    /**
     * @return the name of the command
     */
    public String name() {
        return "";
    }

    @Override
    /**
     * Executes the command by making all hidden meshes visible and clearing the hidden meshes list.
     */
    public void execute() {
        for (MeshView mesh : hiddenMeshes) {
            mesh.setVisible(true);
        }

        hiddenMeshes.clear();
    }

    @Override
    /**
     * Undoes the reset operation by hiding the previously visible meshes and restoring the hidden meshes list.
     */
    public void undo() {
        for (MeshView mesh : backupMeshes) {
            mesh.setVisible(false);
        }

        hiddenMeshes.addAll(backupMeshes);
    }
}
