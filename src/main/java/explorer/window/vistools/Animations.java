package explorer.window.vistools;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.Interpolator;

import java.util.*;

public class Animations {

    // Store original translate positions to allow resetting the explosion
    private final Map<Node, double[]> originalPositions = new HashMap<>();

    private HashSet<Node> currentNodes = new HashSet<>();

    private boolean isExploded = false;

    public void explosion(Group group) {

        HashSet<Node> newNodes = new HashSet<>(group.getChildren());

        // if some meshes are already exploded
        if (isExploded) {
            // either reset all "exploded" meshes if the new group contains only new meshes and continue to animate
            if (completeNewMeshes(newNodes)) {
                resetExplosion();

            // or if some Meshes are already in the "show" than animate an un-explosion and return
            } else {
                animateResetExplosion();
                return;
            }
        }

        // play the animation
        animateExplosion(group);

        isExploded = true;
        currentNodes = newNodes;
    }

    private void animateExplosion(Group group) {
        // Clear any previous stored positions and record current positions
        originalPositions.clear();
        for (Node node : group.getChildren()) {
            originalPositions.put(node, new double[] {
                    node.getTranslateX(),
                    node.getTranslateY(),
                    node.getTranslateZ()
            });
        }
        // Calculate the center of the group
        Bounds boundsOfGroup = group.getBoundsInLocal();
        double centerX = boundsOfGroup.getMinX() + boundsOfGroup.getWidth()  / 2;
        double centerY = boundsOfGroup.getMinY() + boundsOfGroup.getHeight() / 2;
        double centerZ = boundsOfGroup.getMinZ() + boundsOfGroup.getDepth()  / 2;

        Timeline timeline = new Timeline();

        for (Node node : group.getChildren()) {
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

        // start the animation
        timeline.play();
    }

    /**
     * Resets the group to its original positions before explosion.
     */
    private void animateResetExplosion() {
        if (originalPositions.isEmpty()) {
            return; // nothing to reset
        }
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
        isExploded = false;
    }

    /**
     * Resets all child nodes of the given group back to their original
     * translateX/Y/Z values immediately (no animation).
     */
    public void resetExplosion() {
        if (originalPositions.isEmpty()) {
            return; // nothing to reset
        }
        for (Node node : originalPositions.keySet()) {
            double[] pos = originalPositions.get(node);
            if (pos != null) {
                node.setTranslateX(pos[0]);
                node.setTranslateY(pos[1]);
                node.setTranslateZ(pos[2]);
            }
        }

        isExploded = false;
    }

    private boolean completeNewMeshes(HashSet<Node> groupMeshes) {
        if (currentNodes == null || groupMeshes == null) return false;

        return Collections.disjoint(groupMeshes, currentNodes);
    }

    public void pulse(Group group) {

    }
}