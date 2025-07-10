package explorer.window.vistools;

import explorer.window.command.CommandManager;
import explorer.window.command.commands.RotateCaptureCommand;
import explorer.window.vistools.animations.AnimationManager;
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
     * Configures drag-based rotation on the specified pane for the given 3D figure.
     * Records the initial transform on mouse press, applies live rotation on drag,
     * and issues a RotateCaptureCommand on mouse release to capture the entire interaction.
     *
     * @param pane the Pane on which mouse rotation interactions are registered
     * @param figure the Group representing the 3D content to rotate
     * @param commandManager the CommandManager used to execute commands
     * @param animationManager the AnimationManager controlling continuous rotations
     */
    public static void setupMouseRotation(Pane pane, Group figure,
                                          CommandManager commandManager,
                                          AnimationManager animationManager) {

        class InteractionState {
            double mouseX;
            double mouseY;
            Point3D lastAxis;
            Affine beforeRotation;
            Affine afterRotation;
        }
        
        InteractionState interactionState = new InteractionState();

        pane.setOnMousePressed(e -> {
            interactionState.mouseX = e.getSceneX();
            interactionState.mouseY = e.getSceneY();
            interactionState.beforeRotation = new Affine(figure.getTransforms().getFirst());
        });

        pane.setOnMouseDragged(e -> {
            var dx = e.getSceneX() - interactionState.mouseX;
            var dy = e.getSceneY() - interactionState.mouseY;
            var axis = new Point3D(dy, -dx, 0).normalize();
            interactionState.lastAxis = axis;
            var angle = Math.sqrt(dx * dx + dy * dy) * 0.5; // based on the distance of the mouse movement

            applyGlobalRotation(figure, axis, angle);

            interactionState.mouseX = e.getSceneX();
            interactionState.mouseY = e.getSceneY();
        });

        pane.setOnMouseReleased(e -> {
            double rotationChange = 0;

            interactionState.afterRotation = new Affine(figure.getTransforms().getFirst());

            if (interactionState.beforeRotation != null
                    && !interactionState.beforeRotation.equals(interactionState.afterRotation)) {

                double deltaMxx = Math.abs(
                        interactionState.afterRotation.getMxx() - interactionState.beforeRotation.getMxx());
                double deltaMxy = Math.abs(
                        interactionState.afterRotation.getMxy() - interactionState.beforeRotation.getMxy());
                double deltaMxz = Math.abs(
                        interactionState.afterRotation.getMxz() - interactionState.beforeRotation.getMxz());

                double deltaMyx = Math.abs(
                        interactionState.afterRotation.getMyx() - interactionState.beforeRotation.getMyx());
                double deltaMyy = Math.abs(
                        interactionState.afterRotation.getMyy() - interactionState.beforeRotation.getMyy());
                double deltaMyz = Math.abs(
                        interactionState.afterRotation.getMyz() - interactionState.beforeRotation.getMyz());

                double deltaMzx = Math.abs(
                        interactionState.afterRotation.getMzx() - interactionState.beforeRotation.getMzx());
                double deltaMzy = Math.abs(
                        interactionState.afterRotation.getMzy() - interactionState.beforeRotation.getMzy());
                double deltaMzz = Math.abs(
                        interactionState.afterRotation.getMzz() - interactionState.beforeRotation.getMzz());

                rotationChange = deltaMxx + deltaMxy + deltaMxz
                        + deltaMyx + deltaMyy + deltaMyz
                        + deltaMzx + deltaMzy + deltaMzz;
            }

            // simple click will stop any existing rotations.
            boolean stoppedAnimation = animationManager.stopContRotation();

            // guard the executaion of the command so that micro-rotations do not get pushed to the undo stack.
            if (rotationChange > 0.01 && !stoppedAnimation) {
                if (e.isShiftDown() && e.isControlDown()) {
                    animationManager.contRotation(figure, rotationChange,
                                                  interactionState.beforeRotation,
                                                  interactionState.lastAxis);

                } else {
                    commandManager.executeCommand(new RotateCaptureCommand(figure,
                                                                           interactionState.beforeRotation,
                                                                           interactionState.afterRotation));
                }
            }

        });
    }
}
