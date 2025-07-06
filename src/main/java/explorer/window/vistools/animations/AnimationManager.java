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
 * Manages the possible two Animations Explosion and Pulse and ensures their states.
 */
public class AnimationManager {

    private final AtomicReference<Animation> currentExplosionAnimation = new AtomicReference<>(null);
    private final AtomicReference<Animation> currentPulseAnimation = new AtomicReference<>(null);
    private final AtomicReference<Animation> currentContRotation = new AtomicReference<>(null);

    private final CommandManager commandManager;

    public AnimationManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void explosion(Group groupToAnimate, MyCamera camera) {
        if (currentExplosionAnimation.get() == null) {
            ExplosionAnimation explosion = new ExplosionAnimation(groupToAnimate, camera);
            commandManager.executeCommand(new StartAnimationCommand(explosion, currentExplosionAnimation));

        } else if (currentExplosionAnimation.get().isRunning()) {
            commandManager.executeCommand(new StopAnimationCommand(currentExplosionAnimation));
        }
    }

    public void pulse(Group groupToAnimate) {
        if (currentPulseAnimation.get() == null) {
            HashSet<Node> meshesToAnimate = new HashSet<>(groupToAnimate.getChildren());
            PulseAnimation pulse = new PulseAnimation(meshesToAnimate);
            commandManager.executeCommand(new StartAnimationCommand(pulse, currentPulseAnimation));

        } else if (currentPulseAnimation.get().isRunning()) {
            commandManager.executeCommand(new StopAnimationCommand(currentPulseAnimation));
        }
    }

    public void contRotation(Group groupToAnimate, double rotationChange, Affine initialTransform, Point3D rotationAxis) {
        if (currentContRotation.get() == null) {
            ContRotationAnimation contRotationAnimation = new ContRotationAnimation(groupToAnimate, rotationChange, initialTransform, rotationAxis);
            commandManager.executeCommand(new StartAnimationCommand(contRotationAnimation, currentContRotation));
        }
    }

    public boolean stopContRotation() {
        if (currentContRotation.get() != null && currentContRotation.get().isRunning()) {
            StopAnimationCommand stopAnimationCommand = new StopAnimationCommand(currentContRotation);
            commandManager.executeCommand(stopAnimationCommand);
            return true;
        }
        return false;
    }

    public void clearAnimations() {
        commandManager.executeCommand(new ClearAnimationCommand(currentExplosionAnimation, currentPulseAnimation, currentContRotation));
    }
}
