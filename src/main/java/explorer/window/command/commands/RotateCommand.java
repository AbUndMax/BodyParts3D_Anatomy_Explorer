package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.TransformUtils;
import javafx.geometry.Point3D;
import javafx.scene.Group;


/**
 * Command to apply a rotation to the explorer window content group around a specified axis and angle.
 * Provides undo functionality to reverse the applied rotation.
 */
public class RotateCommand implements Command {

    private final Group contentGroup;
    private final Point3D rotAxis;
    private final double rotAngle;

    /**
     * Constructs a RotateCommand with the target group, rotation axis, and rotation angle.
     *
     * @param contentGroup the group to be rotated
     * @param rotAxis the axis of rotation
     * @param rotAngle the angle of rotation in degrees
     */
    public RotateCommand(Group contentGroup, Point3D rotAxis, double rotAngle) {
        this.contentGroup = contentGroup;
        this.rotAxis = rotAxis;
        this.rotAngle = rotAngle;
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Rotation";
    }

    /**
     * Executes the command by applying the rotation to the content group.
     */
    @Override
    public void execute() {
        TransformUtils.applyGlobalRotation(contentGroup, rotAxis, rotAngle);
    }

    /**
     * Undo rotate by applying the inverse of the previously executed rotation.
     */
    @Override
    public void undo() {
        TransformUtils.applyGlobalRotation(contentGroup, rotAxis, -rotAngle);
    }
}
