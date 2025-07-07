package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.animations.Animation;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Command to stop a currently running animation in the explorer window.
 * Provides undo functionality to restart the stopped animation.
 */
public class StopAnimationCommand implements Command {

    private final Animation animation;
    private final AtomicReference<Animation> currentAnimation;

    /**
     * Constructs a StopAnimationCommand with the current animation reference.
     *
     * @param currentAnimation the atomic reference managing the currently active animation
     */
    public StopAnimationCommand(AtomicReference<Animation> currentAnimation) {
        this.animation = currentAnimation.get();
        this.currentAnimation = currentAnimation;
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Animation Stop";
    }

    /**
     * Executes the command by stopping the currently active animation and clearing the animation reference.
     */
    @Override
    public void execute() {
        animation.stop();
        currentAnimation.set(null);
    }

    /**
     * Restarts the previously stopped animation and updates the current animation reference.
     */
    @Override
    public void undo() {
        animation.start();
        currentAnimation.set(animation);
    }
}
