package explorer.window.vistools.animations;

import explorer.window.command.CommandManager;
import explorer.window.command.commands.ClearAnimationCommand;
import explorer.window.command.commands.StartAnimationCommand;
import explorer.window.command.commands.StopAnimationCommand;
import javafx.geometry.Bounds;
import javafx.scene.Node;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages the possible two Animations Explosion and Pulse and ensures their states.
 */
public class AnimationManager {

    private final AtomicReference<Animation> currentExplosionAnimation = new AtomicReference<>(null);
    private final AtomicReference<Animation> currentPulseAnimation = new AtomicReference<>(null);

    private final CommandManager commandManager;

    public AnimationManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void explosion(HashSet<Node> meshesToAnimate, Bounds boundsInLocal) {
        if (currentExplosionAnimation.get() == null) {
            ExplosionAnimation explosion = new ExplosionAnimation(meshesToAnimate, boundsInLocal);
            commandManager.executeCommand(new StartAnimationCommand(explosion, currentExplosionAnimation));

        } else if (currentExplosionAnimation.get().isRunning()) {
            commandManager.executeCommand(new StopAnimationCommand(currentExplosionAnimation));
        }
    }

    public void pulse(HashSet<Node> meshesToAnimate) {
        if (currentPulseAnimation.get() == null) {
            PulseAnimation pulse = new PulseAnimation(meshesToAnimate);
            commandManager.executeCommand(new StartAnimationCommand(pulse, currentPulseAnimation));

        } else if (currentPulseAnimation.get().isRunning()) {
            commandManager.executeCommand(new StopAnimationCommand(currentPulseAnimation));
        }
    }

    public void clearAnimations() {
        commandManager.executeCommand(new ClearAnimationCommand(currentExplosionAnimation, currentPulseAnimation));
    }
}
