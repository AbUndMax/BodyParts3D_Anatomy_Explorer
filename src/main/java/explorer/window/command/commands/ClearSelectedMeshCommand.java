package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.selection.MeshSelectionManager;
import javafx.scene.shape.MeshView;

public class ClearSelectedMeshCommand implements Command {

    private final MeshSelectionManager model;
    private final MeshView mesh;

    public ClearSelectedMeshCommand(MeshSelectionManager model, MeshView mesh) {
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
