package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.selection.MultipleMeshSelectionModel;
import javafx.scene.shape.MeshView;

public class ClearSelectedMeshCommand implements Command {

    private final MultipleMeshSelectionModel model;
    private final MeshView mesh;

    public ClearSelectedMeshCommand(MultipleMeshSelectionModel model, MeshView mesh) {
        this.model = model;
        this.mesh = mesh;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public void execute() {
        model.clearSelection(mesh);
    }

    @Override
    public void undo() {
        model.select(mesh);
    }
}
