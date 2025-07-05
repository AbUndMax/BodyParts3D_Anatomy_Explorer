package explorer.window.vistools.animations;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PulseAnimation implements Animation {

    // Per-node pulse state
    private Map<Node, Timeline> pulseTimelines = new HashMap<>();
    private Map<Node, Scale> pulseScales = new HashMap<>();

    private final HashSet<Node> animatedMeshes;

    public PulseAnimation(HashSet<Node> meshesToAnimate) {
        this.animatedMeshes = meshesToAnimate;
    }

    @Override
    public void play() {
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
            tl.setCycleCount(javafx.animation.Animation.INDEFINITE);
            tl.play();
            pulseTimelines.put(node, tl);
        }
    }

    @Override
    public void reverse() {
        this.stop();
    }

    @Override
    public void stop() {
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
        }
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
