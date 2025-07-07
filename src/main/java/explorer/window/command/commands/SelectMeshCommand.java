package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.selection.MeshSelectionManager;
import javafx.scene.shape.MeshView;


/**
 * Command to select a specific mesh in the explorer window.
 * Provides undo functionality to clear the selection of the previously selected mesh.
 */
public class SelectMeshCommand implements Command {

    private final MeshSelectionManager model;
    private final MeshView mesh;

    /**
     * Constructs a SelectMeshCommand for a specific mesh.
     *
     * @param model the MeshSelectionManager handling selection logic
     * @param mesh the mesh to be selected
     */
    public SelectMeshCommand(MeshSelectionManager model, MeshView mesh) {
        this.model = model;
        this.mesh = mesh;
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Select Mesh";
    }

    /**
     * Executes the command by selecting the target mesh.
     */
    @Override
    public void execute() {
        model.select(mesh);
    }

    /**
     * Undoes the mesh selection by clearing the selection of the previously selected mesh.
     */
    @Override
    public void undo() {
        model.clearSelection(mesh);
    }
}