package explorer.window.vistools;

import explorer.window.command.CommandManager;
import explorer.window.command.commands.RotateCommand;
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
            commandManager.executeCommand(new RotateMemoryCommand(figure, beforeRotation[0], afterRotation[0]));
        });
    }
}
