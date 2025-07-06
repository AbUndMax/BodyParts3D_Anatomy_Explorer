package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.selection.MeshSelectionManager;
import javafx.scene.shape.MeshView;

public class SelectMeshCommand implements Command {

    private final MeshSelectionManager model;
    private final MeshView mesh;

    public SelectMeshCommand(MeshSelectionManager model, MeshView mesh) {
        this.model = model;
        this.mesh = mesh;
    }

    @Override
    public String name() {
        return "Select Mesh";
    }

    @Override
    public void execute() {
        model.select(mesh);
    }

    @Override
    public void undo() {
        model.clearSelection(mesh);
    }
}