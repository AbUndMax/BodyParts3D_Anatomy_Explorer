package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.animations.Animation;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Command to clear and reset animations in the explorer window.
 * This command supports undo functionality to restore animations to their initial states.
 */
public class ClearAnimationCommand implements Command {

    private final AtomicReference<Animation> explosionAnimation;
    public final Animation initialExplosion;
    private final AtomicReference<Animation> pulseAnimation;
    private final Animation initialPulse;
    private final AtomicReference<Animation> contRotateAnimation;
    private final Animation initContRotate;

    /**
     * Constructs a ClearAnimationCommand with references to the animations to be managed.
     *
     * @param explosionAnimation the reference to the explosion animation
     * @param pulseAnimation the reference to the pulse animation
     * @param contRotateAnimation the reference to the continuous rotation animation
     */
    public ClearAnimationCommand(AtomicReference<Animation> explosionAnimation, AtomicReference<Animation> pulseAnimation, AtomicReference<Animation> contRotateAnimation) {
        this.explosionAnimation = explosionAnimation;
        initialExplosion = explosionAnimation.get();
        this.pulseAnimation = pulseAnimation;
        initialPulse = pulseAnimation.get();
        this.contRotateAnimation = contRotateAnimation;
        initContRotate = contRotateAnimation.get();
    }

    @Override
    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String name() {
        return "Animate";
    }

    @Override
    /**
     * Executes the command by resetting and clearing all animations.
     * This will stop and remove the current animations.
     */
    public void execute() {
        if (explosionAnimation.get() != null) {
            explosionAnimation.get().reset();
            explosionAnimation.set(null);
        }

        if (pulseAnimation.get() != null) {
            pulseAnimation.get().reset();
            pulseAnimation.set(null);
        }

        if (contRotateAnimation.get() != null) {
            contRotateAnimation.get().reset();
            contRotateAnimation.set(null);
        }
    }

    @Override
    /**
     * Undoes the clear operation by restoring and restarting the animations
     * to their initial states.
     */
    public void undo() {
        if (explosionAnimation.get() == null && initialExplosion != null) {
            explosionAnimation.set(initialExplosion);
            explosionAnimation.get().start();
        }

        if (pulseAnimation.get() == null && initialPulse != null) {
            pulseAnimation.set(initialPulse);
            pulseAnimation.get().start();
        }

        if (contRotateAnimation.get() == null && initContRotate != null) {
            contRotateAnimation.set(initContRotate);
            contRotateAnimation.get().start();
        }
    }
}
