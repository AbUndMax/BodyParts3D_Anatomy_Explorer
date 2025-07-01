package explorer.window.command.commands;

import explorer.window.command.Command;
import javafx.scene.Group;
import javafx.scene.transform.Affine;

public class RotateMemoryCommand implements Command {

    private final Group contentGroup;
    private final Affine beforeRotation;
    private final Affine afterRotation;

    public RotateMemoryCommand(Group contentGroup, Affine beforeRotation, Affine afterRotation) {
        this.contentGroup = contentGroup;
        this.beforeRotation = beforeRotation;
        this.afterRotation = afterRotation;
    }

    @Override
    public String name() {
        return "Rotation";
    }

    @Override
    public void execute() {
        contentGroup.getTransforms().setAll(afterRotation);
    }

    @Override
    public void undo() {
        contentGroup.getTransforms().setAll(beforeRotation);
    }
}
