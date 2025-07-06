package explorer.window.vistools.animations;

import explorer.window.vistools.TransformUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.Affine;

public class ContRotationAnimation implements Animation {

    private final Group groupToAnimate;
    private final double rotationSpeed;
    private final Affine initialTransform;
    private final Point3D rotationAxis;

    private Timeline rotationTimeline;
    private boolean isRunning = false;

    public ContRotationAnimation(Group groupToAnimate, double rotationChange, Affine initialTransform, Point3D rotationAxis) {
        this.groupToAnimate = groupToAnimate;
        this.rotationSpeed = rotationChange * 2; // speed factor, can be tuned
        this.initialTransform = initialTransform;
        this.rotationAxis = rotationAxis;
    }

    @Override
    public void start() {
        if (rotationTimeline != null) {
            rotationTimeline.stop();
        }

        rotationTimeline = new Timeline(
            new KeyFrame(javafx.util.Duration.seconds(0.016), e -> {
                // Apply a continuous rotation around the Y-axis
                TransformUtils.applyGlobalRotation(groupToAnimate, rotationAxis, rotationSpeed);
            })
        );
        rotationTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        rotationTimeline.play();
        isRunning = true;
    }

    @Override
    public void reverse() {
        if (rotationTimeline != null) {
            rotationTimeline.stop();
            rotationTimeline = null;
        }
        isRunning = false;
    }

    /**
     * resets to state on how the spinning startet
     */
    @Override
    public void stop() {
        if (rotationTimeline != null) {
            rotationTimeline.stop();
            rotationTimeline = null;
        }
        groupToAnimate.getTransforms().clear();
        groupToAnimate.getTransforms().add(initialTransform);
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
