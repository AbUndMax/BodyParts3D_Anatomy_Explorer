package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.animations.Animation;

import java.util.concurrent.atomic.AtomicReference;

public class StopAnimationCommand implements Command {

    private final Animation animation;
    private final AtomicReference<Animation> currentAnimation;

    public StopAnimationCommand(AtomicReference<Animation> currentAnimation) {
        this.animation = currentAnimation.get();
        this.currentAnimation = currentAnimation;
    }

    @Override
    public String name() {
        return "Animation Stop";
    }

    @Override
    public void execute() {
        animation.reverse();
        currentAnimation.set(null);
    }

    @Override
    public void undo() {
        animation.start();
        currentAnimation.set(animation);
    }
}
