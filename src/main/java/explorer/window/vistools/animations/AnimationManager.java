
package explorer.window.vistools.animations;

import explorer.window.command.CommandManager;
import explorer.window.command.commands.ClearAnimationCommand;
import explorer.window.command.commands.StartAnimationCommand;
import explorer.window.command.commands.StopAnimationCommand;
import explorer.window.vistools.MyCamera;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Affine;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages explosion, pulse, and continuous rotation animations in the explorer window.
 * Uses the CommandManager to start and stop animations via commands.
 * Allows toggling individual animations and clearing all animations.
 */
public class AnimationManager {

    // For each animation type only ONE active animation is allowed.
    // if there is no respective animation active, the reference is null
    private final AtomicReference<Animation> currentExplosionAnimation = new AtomicReference<>(null);
    private final AtomicReference<Animation> currentPulseAnimation = new AtomicReference<>(null);
    private final AtomicReference<Animation> currentContRotation = new AtomicReference<>(null);

    private final CommandManager commandManager;

    /**
     * Constructs an AnimationManager with the given CommandManager.
     *
     * @param commandManager the CommandManager used to execute animation commands
     */
    public AnimationManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Toggles the explosion animation: starts a new explosion if none is running,
     * or stops the current explosion animation if it is running.
     *
     * @param groupToAnimate the group to animate
     * @param camera the camera associated with the explosion animation
     */
    public void explosion(Group groupToAnimate, MyCamera camera) {
        if (currentExplosionAnimation.get() == null) {
            ExplosionAnimation explosion = new ExplosionAnimation(groupToAnimate, camera);
            // Start explosion animation via command
            commandManager.executeCommand(new StartAnimationCommand(explosion, currentExplosionAnimation));
        } else if (currentExplosionAnimation.get().isRunning()) {
            // Stop explosion animation via command
            commandManager.executeCommand(new StopAnimationCommand(currentExplosionAnimation));
        }
    }

    /**
     * Toggles the pulse animation on the given group: starts if no pulse is running,
     * or stops the current pulse animation if it is running.
     *
     * @param groupToAnimate the group whose nodes will pulse
     */
    public void pulse(Group groupToAnimate) {
        if (currentPulseAnimation.get() == null) {
            HashSet<Node> meshesToAnimate = new HashSet<>(groupToAnimate.getChildren());
            PulseAnimation pulse = new PulseAnimation(meshesToAnimate);
            // Start pulse animation via command
            commandManager.executeCommand(new StartAnimationCommand(pulse, currentPulseAnimation));
        } else if (currentPulseAnimation.get().isRunning()) {
            // Stop pulse animation via command
            commandManager.executeCommand(new StopAnimationCommand(currentPulseAnimation));
        }
    }

    /**
     * Toggles continuous rotation animation: starts a new continuous rotation if none is running,
     * or stops the current continuous rotation animation if it is running.
     *
     * @param groupToAnimate the group to rotate
     * @param rotationChange the rotation increment amount
     * @param initialTransform the transform state before rotation begins
     * @param rotationAxis the axis around which the rotation occurs
     */
    public void contRotation(Group groupToAnimate, double rotationChange, Affine initialTransform, Point3D rotationAxis) {
        if (currentContRotation.get() == null) {
            ContRotationAnimation contRotationAnimation = new ContRotationAnimation(groupToAnimate, rotationChange,
                                                                                    initialTransform, rotationAxis);
            // Start continuous rotation via command
            commandManager.executeCommand(new StartAnimationCommand(contRotationAnimation, currentContRotation));
        } else if (currentContRotation.get().isRunning()) {
            // Stop continuous rotation via command
            commandManager.executeCommand(new StopAnimationCommand(currentContRotation));
        }
    }

    /**
     * Stops the currently running continuous rotation animation if any.
     *
     * @return true if a continuous rotation animation was running and has been stopped, false otherwise
     */
    public boolean stopContRotation() {
        if (currentContRotation.get() != null && currentContRotation.get().isRunning()) {
            // Execute stop continuous rotation command
            StopAnimationCommand stopAnimationCommand = new StopAnimationCommand(currentContRotation);
            commandManager.executeCommand(stopAnimationCommand);
            return true;
        }
        return false;
    }

    /**
     * Clears and resets all animations by executing a ClearAnimationCommand,
     * stopping any running explosion, pulse, or continuous rotation animations.
     */
    public void clearAnimations() {
        // Clear all animations via command
        commandManager.executeCommand(new ClearAnimationCommand(currentExplosionAnimation,
                                                                currentPulseAnimation,
                                                                currentContRotation));
    }
}
