package explorer.window.vistools;

import explorer.window.command.CommandManager;
import explorer.window.command.commands.RotateMemoryCommand;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Utility class for functions that apply transformations on javafx Group
 */
public class TransformUtils {

    /**
     * Applies a global rotation transformation to the specified 3D content group.
     * This method rotates the group around the given axis by the specified angle
     * and updates its transformation accordingly.
     *
     * @param contentGroup The group of 3D content to which the rotation transformation
     *                     will be applied.
     * @param axis The axis around which the 3D content group will be rotated, represented
     *             as a {@code Point3D}.
     * @param angle The rotation angle in degrees to apply to the 3D content group.
     *
     * SOURCE: copy from assignment06 WindowPresenter class
     */
    public static void applyGlobalRotation(Group contentGroup, Point3D axis, double angle) {
        var currentTransform = contentGroup.getTransforms().getFirst();
        var rotate = new Rotate(angle, axis);
        currentTransform = rotate.createConcatenation(currentTransform);
        contentGroup.getTransforms().setAll(currentTransform);
    }

    /**
     * Centers the specified 3D group to its own local bounding box.
     * This method calculates the center point of the group's local bounds
     * and applies a translation to shift the group so that its center aligns with the origin.
     *
     * @param group The 3D group to be centered relative to its local coordinate system.
     *
     * SOURCE: copy from assignment06 ObjIO class
     */
    public static void centerGroupToItself(Group group) {
        Bounds bounds = group.getBoundsInLocal();
        double X = (bounds.getMinX() + bounds.getMaxX()) / 2;
        double Y = (bounds.getMinY() + bounds.getMaxY()) / 2;
        double Z = (bounds.getMinZ() + bounds.getMaxZ()) / 2;
        group.getTransforms().setAll(new Translate(-X, -Y, -Z));
    }

    /**
     * setups the mouse rotation functionality on:
     * @param pane in which the mouse rotation should be active
     * @param figure on which the rotation should be applied
     *
     * SOURCE: modified based on assignment06 MouseRotate3D class
     */
    public static void setupMouseRotation(Pane pane, Group figure, CommandManager commandManager) {
        final double[] xPrev = new double[1];
        final double[] yPrev = new double[1];
        final Affine[] beforeRotation = new Affine[1];
        final Affine[] afterRotation = new Affine[1];

        pane.setOnMousePressed(e -> {
            xPrev[0] = e.getSceneX();
            yPrev[0] = e.getSceneY();
            beforeRotation[0] = new Affine(figure.getTransforms().getFirst());
        });

        pane.setOnMouseDragged(e -> {
            var dx = e.getSceneX() - xPrev[0];
            var dy = e.getSceneY() - yPrev[0];
            var axis = new Point3D(dy, -dx, 0).normalize();
            var angle = Math.sqrt(dx * dx + dy * dy) * 0.5; // based on the distance of the mouse movement

            applyGlobalRotation(figure, axis, angle);

            xPrev[0] = e.getSceneX();
            yPrev[0] = e.getSceneY();
        });

        pane.setOnMouseReleased(e -> {
            afterRotation[0] = new Affine(figure.getTransforms().getFirst());
            if (beforeRotation[0] != null && !beforeRotation[0].equals(afterRotation[0])) {
                double deltaMxx = Math.abs(afterRotation[0].getMxx() - beforeRotation[0].getMxx());
                double deltaMxy = Math.abs(afterRotation[0].getMxy() - beforeRotation[0].getMxy());
                double deltaMxz = Math.abs(afterRotation[0].getMxz() - beforeRotation[0].getMxz());

                double deltaMyx = Math.abs(afterRotation[0].getMyx() - beforeRotation[0].getMyx());
                double deltaMyy = Math.abs(afterRotation[0].getMyy() - beforeRotation[0].getMyy());
                double deltaMyz = Math.abs(afterRotation[0].getMyz() - beforeRotation[0].getMyz());

                double deltaMzx = Math.abs(afterRotation[0].getMzx() - beforeRotation[0].getMzx());
                double deltaMzy = Math.abs(afterRotation[0].getMzy() - beforeRotation[0].getMzy());
                double deltaMzz = Math.abs(afterRotation[0].getMzz() - beforeRotation[0].getMzz());

                double rotationChange = deltaMxx + deltaMxy + deltaMxz
                        + deltaMyx + deltaMyy + deltaMyz
                        + deltaMzx + deltaMzy + deltaMzz;

                // guard the executaion of the command so that micro-rotations do not get pushed to the undo stack.
                if (rotationChange > 0.1) {
                    commandManager.executeCommand(new RotateMemoryCommand(figure, beforeRotation[0], afterRotation[0]));
                }
            }
        });
    }
}
