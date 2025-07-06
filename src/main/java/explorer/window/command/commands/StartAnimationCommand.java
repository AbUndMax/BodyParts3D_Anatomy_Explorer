package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.animations.Animation;

import java.util.concurrent.atomic.AtomicReference;

public class StartAnimationCommand implements Command {

    private final Animation animation;
    private final AtomicReference<Animation> currentAnimation;

    public StartAnimationCommand(Animation animation, AtomicReference<Animation> currentAnimation) {
        this.animation = animation;
        this.currentAnimation = currentAnimation;
    }

    @Override
    public String name() {
        return "Animation Start";
    }

    @Override
    public void execute() {
        currentAnimation.set(animation);
        animation.start();
    }

    @Override
    public void undo() {
        animation.stop();
        currentAnimation.set(null);
    }
}
