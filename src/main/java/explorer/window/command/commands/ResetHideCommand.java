package explorer.window.command.commands;

import explorer.window.command.Command;
import javafx.scene.shape.MeshView;

import java.util.LinkedList;

public class ResetHideCommand implements Command {

    private final LinkedList<MeshView> hiddenMeshes;
    private final LinkedList<MeshView> backupMeshes;

    public ResetHideCommand(LinkedList<MeshView> hiddenMeshes) {
        this.hiddenMeshes = hiddenMeshes;
        this.backupMeshes = new LinkedList<>(hiddenMeshes);
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public void execute() {
        for (MeshView mesh : hiddenMeshes) {
            mesh.setVisible(true);
        }

        hiddenMeshes.clear();
    }

    @Override
    public void undo() {
        for (MeshView mesh : backupMeshes) {
            mesh.setVisible(false);
        }

        hiddenMeshes.addAll(backupMeshes);
    }
}
