package explorer.window.vistools.animations;

import explorer.window.vistools.MyCamera;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Animation that causes meshes in a 3D group to "explode" outward from their center.
 * Captures original mesh positions, computes explosion trajectories, and animates movement.
 * Allows resetting and stopping the explosion for undo/reset functionality.
 */
public class ExplosionAnimation implements Animation {

    private final Group groupToAnimate;
    private final HashSet<Node> animatedMeshes;
    private final Bounds boundsOfGroup;
    private final MyCamera camera;
    private final ChangeListener<Bounds> boundsListener;

    private boolean isRunning = false;

    // Store original translate positions to allow resetting the explosion
    private final Map<Node, double[]> originalPositions = new HashMap<>();

    /**
     * Constructs an ExplosionAnimation for the specified group and camera.
     *
     * @param groupToAnimate the Group containing meshes to animate
     * @param camera the camera to refocus on the group bounds during animation
     */
    public ExplosionAnimation(Group groupToAnimate, MyCamera camera) {
        this.groupToAnimate = groupToAnimate;
        this.animatedMeshes = new HashSet<>(groupToAnimate.getChildren());
        this.boundsOfGroup = groupToAnimate.getBoundsInLocal();
        this.camera = camera;
        boundsListener = (obs, oldBounds, newBounds) -> {
            camera.focusFullFigure(groupToAnimate);
        };
    }

    /**
     * Starts the explosion animation.
     * Records original positions, computes trajectories away from the group's center,
     * and plays a Timeline that moves each mesh over two seconds.
     * Adds a bounds listener to refocus the camera.
     */
    @Override
    public void start() {
        // Add listener to keep camera focused on exploding group
        groupToAnimate.boundsInParentProperty().addListener(boundsListener);

        // Clear and record mesh original positions for reset
        originalPositions.clear();
        for (Node node : animatedMeshes) {
            originalPositions.put(node, new double[] {
                    node.getTranslateX(),
                    node.getTranslateY(),
                    node.getTranslateZ()
            });
        }
        // Compute center of group for explosion origin
        double centerX = boundsOfGroup.getMinX() + boundsOfGroup.getWidth()  / 2;
        double centerY = boundsOfGroup.getMinY() + boundsOfGroup.getHeight() / 2;
        double centerZ = boundsOfGroup.getMinZ() + boundsOfGroup.getDepth()  / 2;

        Timeline timeline = new Timeline();

        for (Node node : animatedMeshes) {
            // For each mesh, calculate its explosion direction and target position
            // Compute the bounds of the current Node in the loop
            Bounds boundsOfNode = node.getBoundsInParent();
            double nodeX = boundsOfNode.getMinX() + boundsOfNode.getWidth()  / 2;
            double nodeY = boundsOfNode.getMinY() + boundsOfNode.getHeight() / 2;
            double nodeZ = boundsOfNode.getMinZ() + boundsOfNode.getDepth()  / 2;

            // Calculation of the direction vector in which the node will "fly"
            double dx = nodeX - centerX;
            double dy = nodeY - centerY;
            double dz = nodeZ - centerZ;
            double length = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (length == 0) {
                // Even if it is unlikely there is one special case in which the node can be exactly in the center
                // and thus the direction vectors length will get 0. To prevent "division by 0" errors we take a random
                // calculated direction
                dx = Math.random() - 0.5;
                dy = Math.random() - 0.5;
                dz = Math.random() - 0.5;
                length = Math.sqrt(dx*dx + dy*dy + dz*dz);
            }
            double dirX = dx / length, dirY = dy / length, dirZ = dz / length;

            // calculate the longest edge of the group and use it as travel distance:
            double distance = Math.max(boundsOfGroup.getWidth(),
                                       Math.max(boundsOfGroup.getHeight(), boundsOfGroup.getDepth()));

            // then calculate the new coordinates:
            double toX = node.getTranslateX() + dirX * distance;
            double toY = node.getTranslateY() + dirY * distance;
            double toZ = node.getTranslateZ() + dirZ * distance;

            // finally prepare the animation
            KeyValue kvX = new KeyValue(node.translateXProperty(), toX, Interpolator.EASE_OUT);
            KeyValue kvY = new KeyValue(node.translateYProperty(), toY, Interpolator.EASE_OUT);
            KeyValue kvZ = new KeyValue(node.translateZProperty(), toZ, Interpolator.EASE_OUT);
            KeyFrame kf = new KeyFrame(Duration.seconds(2.0), kvX, kvY, kvZ);
            timeline.getKeyFrames().add(kf);
        }

        // Play the explosion animation
        timeline.play();

        // When animation finishes, remove listener and mark as running
        timeline.setOnFinished(e -> {
            groupToAnimate.boundsInParentProperty().removeListener(boundsListener);
            isRunning = true;
        });
    }

    /**
     * Resets all meshes to their original positions.
     * Stops any running explosion and reapplies stored positions.
     */
    @Override
    public void reset() {
        groupToAnimate.boundsInParentProperty().addListener(boundsListener);
        if (originalPositions.isEmpty()) {
            return; // nothing to reset
        }
        // Restore each mesh to its recorded original position
        for (Node node : originalPositions.keySet()) {
            double[] pos = originalPositions.get(node);
            if (pos != null) {
                node.setTranslateX(pos[0]);
                node.setTranslateY(pos[1]);
                node.setTranslateZ(pos[2]);
            }
        }
        groupToAnimate.boundsInParentProperty().removeListener(boundsListener);
        isRunning = false;
    }

    /**
     * Stops the explosion and animates meshes back into place over one second.
     */
    @Override
    public void stop() {
        groupToAnimate.boundsInParentProperty().addListener(boundsListener);
        if (originalPositions.isEmpty()) {
            return; // nothing to reset
        }
        // Animate meshes back to original positions with easing
        Timeline resetTimeline = new Timeline();
        for (Node node : originalPositions.keySet()) {
            double[] pos = originalPositions.get(node);
            if (pos == null) {
                continue;
            }
            KeyValue kvX = new KeyValue(node.translateXProperty(), pos[0], Interpolator.EASE_IN);
            KeyValue kvY = new KeyValue(node.translateYProperty(), pos[1], Interpolator.EASE_IN);
            KeyValue kvZ = new KeyValue(node.translateZProperty(), pos[2], Interpolator.EASE_IN);
            KeyFrame kf = new KeyFrame(Duration.seconds(1.0), kvX, kvY, kvZ);
            resetTimeline.getKeyFrames().add(kf);
        }
        resetTimeline.play();

        resetTimeline.setOnFinished(e -> {
            groupToAnimate.boundsInParentProperty().removeListener(boundsListener);
            isRunning = false;
        });
    }

    /**
     * Indicates whether the explosion animation has completed.
     *
     * @return true if the animation has finished, false otherwise
     */
    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
