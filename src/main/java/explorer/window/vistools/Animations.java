package explorer.window.vistools;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.Interpolator;
import javafx.animation.Animation;
import javafx.scene.transform.Scale;

import java.util.*;

public class Animations {

    // Store original translate positions to allow resetting the explosion
    private final Map<Node, double[]> originalPositions = new HashMap<>();

    private HashSet<Node> currentExplodingNodes = new HashSet<>();

    private HashSet<Node> currentpulsingNodes = new HashSet<>();

    private boolean isExploded = false;
    private boolean isPulsating = false;

    // Per-node pulse state
    private Map<Node, Timeline> pulseTimelines = new HashMap<>();
    private Map<Node, Scale> pulseScales = new HashMap<>();

    public void explosion(Group group) {

        HashSet<Node> newNodes = new HashSet<>(group.getChildren());

        // if some meshes are already exploded
        if (isExploded) {
            // either reset all "exploded" meshes if the new group contains only new meshes and continue to animate
            if (completeNewMeshes(currentExplodingNodes, newNodes)) {
                resetExplosion();

            // or if some Meshes are already in the "show" than animate an un-explosion and return
            } else {
                animateResetExplosion();
                return;
            }
        }

        // play the animation
        animateExplosion(group);

        currentExplodingNodes = newNodes;
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
            Bounds boundsOfNode = node.getBoundsInLocal();
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
        isExploded = true;
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

    private boolean completeNewMeshes(HashSet<Node> currentNodes, HashSet<Node> groupMeshes) {
        if (currentNodes == null || groupMeshes == null) return false;

        return Collections.disjoint(groupMeshes, currentNodes);
    }

    public void pulse(Group group) {

        HashSet<Node> newNodes = new HashSet<>(group.getChildren());

        // Toggle off existing per-node animations
        if (isPulsating) {

            // if the shown Meshes are not completely new (i.e pulsating meshes are still shown)
            // then just end the puls, otherwise we stop the pulse (of not shown Meshes) and directly start
            // new pulse of shown meshes. This prevents to have to "double" klick the animate button when
            // new Meshes are shown while the ones that were dismissed are stoll pulsing!
            if (completeNewMeshes(currentpulsingNodes, newNodes)) {
                stopPulsating();
                return;
            } else {
                stopPulsating();
            }
        }

        animatePulse(group);

        currentpulsingNodes = newNodes;
    }

    private void animatePulse(Group group) {
        // Find maximum mesh size to normalize pulse factors
        double maxSize = 0;
        Map<Node, Double> sizes = new HashMap<>();
        for (Node node : group.getChildren()) {
            Bounds nb = node.getBoundsInLocal();
            double diag = Math.sqrt(nb.getWidth()*nb.getWidth()
                                            + nb.getHeight()*nb.getHeight()
                                            + nb.getDepth()*nb.getDepth());
            sizes.put(node, diag);
            if (diag > maxSize) maxSize = diag;
        }
        if (maxSize == 0) maxSize = 1; // avoid division by zero

        // Start per-node pulse animations
        for (Node node : group.getChildren()) {
            double size = sizes.get(node);
            double pulseFactor = 1.0 + (size / maxSize) * 0.2; // up to +20% for largest

            // determine pivot for this mesh
            Bounds nb = node.getBoundsInLocal();
            double px = nb.getMinX() + nb.getWidth()/2;
            double py = nb.getMinY() + nb.getHeight()/2;
            double pz = nb.getMinZ() + nb.getDepth()/2;

            // create and attach scale transform
            Scale sc = new Scale(1, 1, 1, px, py, pz);
            node.getTransforms().add(sc);
            pulseScales.put(node, sc);

            // build timeline
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO,
                                 new KeyValue(sc.xProperty(), 1),
                                 new KeyValue(sc.yProperty(), 1),
                                 new KeyValue(sc.zProperty(), 1)
                    ),
                    new KeyFrame(Duration.seconds(1),
                                 new KeyValue(sc.xProperty(), pulseFactor, Interpolator.EASE_BOTH),
                                 new KeyValue(sc.yProperty(), pulseFactor, Interpolator.EASE_BOTH),
                                 new KeyValue(sc.zProperty(), pulseFactor, Interpolator.EASE_BOTH)
                    ),
                    new KeyFrame(Duration.seconds(2),
                                 new KeyValue(sc.xProperty(), 1, Interpolator.EASE_BOTH),
                                 new KeyValue(sc.yProperty(), 1, Interpolator.EASE_BOTH),
                                 new KeyValue(sc.zProperty(), 1, Interpolator.EASE_BOTH)
                    )
            );
            tl.setCycleCount(Animation.INDEFINITE);
            tl.play();
            pulseTimelines.put(node, tl);
        }

        isPulsating = true;
    }

    private void stopPulsating() {
        if (!pulseTimelines.isEmpty()) {
            for (Node node : pulseTimelines.keySet()) {
                pulseTimelines.get(node).stop();
                Scale sc = pulseScales.get(node);
                if (sc != null) {
                    node.getTransforms().remove(sc);
                }
            }
            pulseTimelines.clear();
            pulseScales.clear();
            currentpulsingNodes.clear();
        }

        isPulsating = false;
    }
}