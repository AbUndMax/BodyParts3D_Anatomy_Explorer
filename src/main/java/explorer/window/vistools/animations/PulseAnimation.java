package explorer.window.vistools.animations;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Animation that makes meshes pulse (scale up and down) continuously.
 * Uses a Timeline per mesh with scaling based on mesh size.
 * Allows resetting and stopping the pulse animation.
 */
public class PulseAnimation implements Animation {

    // Holds per-node pulse state
    private final Map<Node, PulseState> pulseStates = new HashMap<>();

    // Encapsulates a node's pulse Scale and Timeline.
    private record PulseState (Scale scale, Timeline timeline) {}

    private final HashSet<Node> animatedMeshes;

    private boolean isRunning = false;

    /**
     * Constructs a PulseAnimation for the given set of meshes.
     *
     * @param meshesToAnimate the nodes to animate with pulse effect
     */
    public PulseAnimation(HashSet<Node> meshesToAnimate) {
        this.animatedMeshes = meshesToAnimate;
    }

    /**
     * Starts the pulse animation by computing a size-based pulse factor for each mesh,
     * attaching a Scale transform at the mesh's center, and playing a looping Timeline.
     */
    @Override
    public void start() {
        // Compute maximum mesh diagonal to normalize pulse factors
        double maxSize = 0;
        Map<Node, Double> sizes = new HashMap<>();
        for (Node node : animatedMeshes) {
            Bounds nb = node.getBoundsInLocal();
            double diag = Math.sqrt(nb.getWidth()*nb.getWidth()
                                            + nb.getHeight()*nb.getHeight()
                                            + nb.getDepth()*nb.getDepth());
            sizes.put(node, diag);
            if (diag > maxSize) maxSize = diag;
        }
        if (maxSize == 0) maxSize = 1; // avoid division by zero

        // Initialize and play individual pulse animation for each mesh
        for (Node node : animatedMeshes) {
            double size = sizes.get(node);
            double pulseFactor = 1.0 + (size / maxSize) * 0.2; // up to +20% for largest

            // Determine pivot point for mesh scaling
            Bounds nb = node.getBoundsInLocal();
            double px = nb.getMinX() + nb.getWidth()/2;
            double py = nb.getMinY() + nb.getHeight()/2;
            double pz = nb.getMinZ() + nb.getDepth()/2;

            // create and attach scale transform
            Scale scale = new Scale(1, 1, 1, px, py, pz);
            node.getTransforms().add(scale);

            // Create and configure scaling Timeline for the mesh
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                                 new KeyValue(scale.xProperty(), 1),
                                 new KeyValue(scale.yProperty(), 1),
                                 new KeyValue(scale.zProperty(), 1)
                    ),
                    new KeyFrame(Duration.seconds(1),
                                 new KeyValue(scale.xProperty(), pulseFactor, Interpolator.EASE_BOTH),
                                 new KeyValue(scale.yProperty(), pulseFactor, Interpolator.EASE_BOTH),
                                 new KeyValue(scale.zProperty(), pulseFactor, Interpolator.EASE_BOTH)
                    ),
                    new KeyFrame(Duration.seconds(2),
                                 new KeyValue(scale.xProperty(), 1, Interpolator.EASE_BOTH),
                                 new KeyValue(scale.yProperty(), 1, Interpolator.EASE_BOTH),
                                 new KeyValue(scale.zProperty(), 1, Interpolator.EASE_BOTH)
                    )
            );
            timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            timeline.play();
            pulseStates.put(node, new PulseState(scale, timeline));
        }
        isRunning = true;
    }

    /**
     * Resets the pulse animation by stopping all timelines and removing scale transforms.
     */
    @Override
    public void reset() {
        // Stop pulse animation and clear all state
        stop();
    }

    /**
     * Stops the pulse animation, removing all scale transforms and stopping timelines.
     */
    @Override
    public void stop() {
        // Stop each mesh's pulse timeline and remove its scale transform
        for (Node node : animatedMeshes) {
            PulseState state = pulseStates.get(node);
            state.timeline.stop();
            node.getTransforms().remove(state.scale);
        }
        pulseStates.clear();
        isRunning = false;
    }

    /**
     * Indicates whether the pulse animation is currently running.
     *
     * @return true if the animation is active, false otherwise
     */
    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
