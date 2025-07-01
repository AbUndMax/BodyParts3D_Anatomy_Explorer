package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.TransformUtils;
import javafx.geometry.Point3D;
import javafx.scene.Group;

public class RotateCommand implements Command {

    private final Group contentGroup;
    private final Point3D rotAxis;
    private final double rotAngle;

    public RotateCommand(Group contentGroup, Point3D rotAxis, double rotAngle) {
        this.contentGroup = contentGroup;
        this.rotAxis = rotAxis;
        this.rotAngle = rotAngle;
    }

    @Override
    public String name() {
        return "Rotation";
    }

    @Override
    public void execute() {
        TransformUtils.applyGlobalRotation(contentGroup, rotAxis, rotAngle);
    }

    @Override
    public void undo() {
        TransformUtils.applyGlobalRotation(contentGroup, rotAxis, -rotAngle);
    }
}
