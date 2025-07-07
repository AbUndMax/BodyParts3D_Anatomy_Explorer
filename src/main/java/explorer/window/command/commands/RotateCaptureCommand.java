package explorer.window.command.commands;

import explorer.window.command.Command;
import javafx.scene.Group;
import javafx.scene.transform.Affine;


/**
 * Command to apply a saved rotation to the explorer window content group.
 * Provides undo functionality to revert to the previous transform state.
 *
 * The Command "captures" the initial and final transformations of the object
 * during a drag-based rotation and encapsulates the whole interaction in one undoable command.
 */
public class RotateCaptureCommand implements Command {

    private final Group contentGroup;
    private final Affine beforeRotation;
    private final Affine afterRotation;

    /**
     * Constructs a RotateMemoryCommand with the content group and its before and after rotation states.
     *
     * @param contentGroup the group whose transform will be updated
     * @param beforeRotation the transform state before rotation
     * @param afterRotation the transform state after rotation
     */
    public RotateCaptureCommand(Group contentGroup, Affine beforeRotation, Affine afterRotation) {
        this.contentGroup = contentGroup;
        this.beforeRotation = beforeRotation;
        this.afterRotation = afterRotation;
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Rotation";
    }

    /**
     * Executes the command by applying the after-rotation transform to the content group.
     */
    @Override
    public void execute() {
        contentGroup.getTransforms().setAll(afterRotation);
    }

    /**
     * Undo the content group's transform to its state before rotation.
     */
    @Override
    public void undo() {
        contentGroup.getTransforms().setAll(beforeRotation);
    }
}
