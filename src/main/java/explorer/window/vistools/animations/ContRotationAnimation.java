package explorer.window.vistools.animations;

import explorer.window.vistools.TransformUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.Affine;

/**
 * Animation that continuously rotates a 3D group around a specified axis.
 * Uses a Timeline to apply incremental rotations at a fixed interval.
 * The initial transform is captured for reset functionality.
 */
public class ContRotationAnimation implements Animation {

    private final Group groupToAnimate;
    private final double rotationSpeed;
    private final Affine initialTransform;
    private final Point3D rotationAxis;

    private Timeline rotationTimeline;
    private boolean isRunning = false;

    /**
     * Constructs a continuous rotation animation.
     *
     * @param groupToAnimate the Group to rotate
     * @param rotationChange the rotation increment per frame (degrees per tick)
     * @param initialTransform the initial Affine transform to restore on reset
     * @param rotationAxis the axis around which the group will rotate
     */
    public ContRotationAnimation(Group groupToAnimate, double rotationChange,
                                 Affine initialTransform, Point3D rotationAxis) {

        this.groupToAnimate = groupToAnimate;
        this.rotationSpeed = rotationChange * 2; // *2 is just some finetuning in the speed
        this.initialTransform = initialTransform;
        this.rotationAxis = rotationAxis;
    }

    /**
     * Starts the continuous rotation animation.
     * Initializes and plays a Timeline that applies global rotation at ~60 FPS.
     * Marks the animation as running.
     */
    @Override
    public void start() {
        // Stop any existing timeline before starting a new one
        if (rotationTimeline != null) {
            rotationTimeline.stop();
        }

        // Schedule rotation frames at approximately 60 frames per second
        rotationTimeline = new Timeline(
            new KeyFrame(javafx.util.Duration.seconds(0.016), e -> {
                // Apply a continuous rotation around the Y-axis
                TransformUtils.applyGlobalRotation(groupToAnimate, rotationAxis, rotationSpeed);
            })
        );

        // Loop the rotation indefinitely
        rotationTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);

        // Begin the rotation animation
        rotationTimeline.play();
        isRunning = true;
    }

    /**
     * Resets the animation to its initial state.
     * Stops any running animation, clears transforms, and reapplies the initial transform.
     */
    @Override
    public void reset() {
        // Stop current animation and restore original transform
        stop();
        groupToAnimate.getTransforms().clear();
        groupToAnimate.getTransforms().add(initialTransform);
        isRunning = false;
    }

    /**
     * Stops the continuous rotation animation without resetting transforms.
     * Cancels the Timeline and clears its reference.
     */
    @Override
    public void stop() {
        // Stop the timeline and release resources
        if (rotationTimeline != null) {
            rotationTimeline.stop();
            rotationTimeline = null;
        }
    }

    /**
     * Indicates whether the animation is currently running.
     *
     * @return true if the animation timeline is active, false otherwise
     */
    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
