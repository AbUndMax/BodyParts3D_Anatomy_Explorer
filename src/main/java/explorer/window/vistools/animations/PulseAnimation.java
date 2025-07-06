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

public class PulseAnimation implements Animation {

    // Holds per-node pulse state
    private final Map<Node, PulseState> pulseStates = new HashMap<>();

    // Encapsulates a node's pulse Scale and Timeline.
    private record PulseState (Scale scale, Timeline timeline) {}

    private final HashSet<Node> animatedMeshes;

    private boolean isRunning = false;

    public PulseAnimation(HashSet<Node> meshesToAnimate) {
        this.animatedMeshes = meshesToAnimate;
    }

    @Override
    public void start() {
        // Find maximum mesh size to normalize pulse factors
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

        // Start per-node pulse animations
        for (Node node : animatedMeshes) {
            double size = sizes.get(node);
            double pulseFactor = 1.0 + (size / maxSize) * 0.2; // up to +20% for largest

            // determine pivot for this mesh
            Bounds nb = node.getBoundsInLocal();
            double px = nb.getMinX() + nb.getWidth()/2;
            double py = nb.getMinY() + nb.getHeight()/2;
            double pz = nb.getMinZ() + nb.getDepth()/2;

            // create and attach scale transform
            Scale scale = new Scale(1, 1, 1, px, py, pz);
            node.getTransforms().add(scale);

            // build timeline
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

    @Override
    public void reset() {
        this.stop();
    }

    @Override
    public void stop() {
        for (Node node : animatedMeshes) {
            PulseState state = pulseStates.get(node);
            state.timeline.stop();
            node.getTransforms().remove(state.scale);
        }
        pulseStates.clear();
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
