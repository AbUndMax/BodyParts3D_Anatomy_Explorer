package explorer.window.command.commands;

import explorer.window.command.Command;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;


/**
 * Command to hide a specific mesh in the explorer window.
 * Provides undo functionality to make the mesh visible again.
 * Tracks hidden meshes in a shared list.
 */
public class HideMeshCommand implements Command {

    private final MeshView mesh;
    private final ArrayList<MeshView> hiddenMeshes;

    /**
     * Constructs a HideMeshCommand for a specific mesh.
     *
     * @param mesh the mesh to hide
     * @param hiddenMeshes the list tracking all hidden meshes
     */
    public HideMeshCommand(MeshView mesh, ArrayList<MeshView> hiddenMeshes) {
        this.mesh = mesh;
        this.hiddenMeshes = hiddenMeshes;
    }

    @Override
    /**
     * @return the name of the command
     */
    public String name() {
        return "Hide Mesh";
    }

    @Override
    /**
     * Executes the command by hiding the target mesh and adding it to the hidden meshes list.
     */
    public void execute() {
        mesh.setVisible(false);
        hiddenMeshes.add(mesh);
    }

    @Override
    /**
     * Undoes the hide operation by making the mesh visible again and removing it from the hidden meshes list.
     */
    public void undo() {
        mesh.setVisible(true);
        hiddenMeshes.remove(mesh);
    }
}
