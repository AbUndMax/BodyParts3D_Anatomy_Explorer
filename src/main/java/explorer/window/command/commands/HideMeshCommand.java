package explorer.window.command.commands;

import explorer.window.command.Command;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;

public class HideMeshCommand implements Command {

    private final MeshView mesh;
    private final ArrayList<MeshView> hiddenMeshes;

    public HideMeshCommand(MeshView mesh, ArrayList<MeshView> hiddenMeshes) {
        this.mesh = mesh;
        this.hiddenMeshes = hiddenMeshes;
    }

    @Override
    public String name() {
        return "Hide Mesh";
    }

    @Override
    public void execute() {
        mesh.setVisible(false);
        hiddenMeshes.add(mesh);
    }

    @Override
    public void undo() {
        mesh.setVisible(true);
        hiddenMeshes.remove(mesh);
    }
}
