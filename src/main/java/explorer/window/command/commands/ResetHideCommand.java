package explorer.window.command.commands;

import explorer.window.command.Command;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;

public class ResetHideCommand implements Command {

    private final ArrayList<MeshView> hiddenMeshes;
    private final ArrayList<MeshView> backupMeshes;

    public ResetHideCommand(ArrayList<MeshView> hiddenMeshes) {
        this.hiddenMeshes = hiddenMeshes;
        this.backupMeshes = new ArrayList<>(hiddenMeshes);
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
