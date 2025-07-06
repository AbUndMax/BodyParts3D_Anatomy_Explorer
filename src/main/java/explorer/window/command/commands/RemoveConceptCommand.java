package explorer.window.command.commands;

import explorer.window.command.Command;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;

import java.util.List;

public class RemoveConceptCommand implements Command {

    private final List<MeshView> meshesToRemove;
    private final Group anatomyGroup;

    public RemoveConceptCommand(List<MeshView> meshesToRemove, Group anatomyGroup) {
        this.meshesToRemove = meshesToRemove;
        this.anatomyGroup = anatomyGroup;
    }

    @Override
    public String name() {
        return "Remove Concept";
    }

    @Override
    public void execute() {
        anatomyGroup.getChildren().removeAll(meshesToRemove);
    }

    @Override
    public void undo() {
        anatomyGroup.getChildren().addAll(meshesToRemove);
    }
}
