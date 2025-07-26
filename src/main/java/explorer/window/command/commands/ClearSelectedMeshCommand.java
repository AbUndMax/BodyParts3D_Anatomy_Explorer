package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.selection.MeshSelectionManager;
import javafx.scene.shape.MeshView;

/**
 * Command to clear the selection of a specific mesh in the explorer window.
 * Provides undo functionality to reselect the cleared mesh.
 */
public class ClearSelectedMeshCommand implements Command {

    private final MeshSelectionManager model;
    private final MeshView mesh;

    /**
     * Constructs a ClearSelectedMeshCommand for a specific mesh.
     *
     * @param model the MeshSelectionManager handling the selection logic
     * @param mesh the mesh to be deselected
     */
    public ClearSelectedMeshCommand(MeshSelectionManager model, MeshView mesh) {
        this.model = model;
        this.mesh = mesh;
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
     * Executes the command by clearing the selection of the target mesh.
     */
    public void execute() {
        model.deselect(mesh);
    }

    @Override
    /**
     * Undoes the deselection by reselecting the previously cleared mesh.
     */
    public void undo() {
        model.select(mesh);
    }
}
