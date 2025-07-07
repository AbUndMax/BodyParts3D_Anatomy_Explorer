package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.animations.Animation;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Command to start a specific animation in the explorer window.
 * Provides undo functionality to stop the animation and reset it to its initial state.
 */
public class StartAnimationCommand implements Command {

    private final Animation animation;
    private final AtomicReference<Animation> currentAnimation;

    /**
     * Constructs a StartAnimationCommand with the animation to start and its associated reference holder.
     *
     * @param animation the animation to start
     * @param currentAnimation the atomic reference managing the currently active animation
     */
    public StartAnimationCommand(Animation animation, AtomicReference<Animation> currentAnimation) {
        this.animation = animation;
        this.currentAnimation = currentAnimation;
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Animation Start";
    }

    /**
     * Executes the command by starting the specified animation and updating the reference.
     */
    @Override
    public void execute() {
        currentAnimation.set(animation);
        animation.start();
    }

    /**
     * Stops and resets the animation, and clears the current animation reference to undo the start action.
     */
    @Override
    public void undo() {
        animation.reset();
        currentAnimation.set(null);
    }
}
