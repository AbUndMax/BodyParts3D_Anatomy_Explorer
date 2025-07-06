package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.animations.Animation;

import java.util.concurrent.atomic.AtomicReference;

public class ClearAnimationCommand implements Command {

    private final AtomicReference<Animation> explosionAnimation;
    public final Animation initialExplosion;
    private final AtomicReference<Animation> pulseAnimation;
    private final Animation initalPulse;

    public ClearAnimationCommand(AtomicReference<Animation> explosionAnimation, AtomicReference<Animation> pulseAnimation) {
        this.explosionAnimation = explosionAnimation;
        initialExplosion = explosionAnimation.get();
        this.pulseAnimation = pulseAnimation;
        initalPulse = pulseAnimation.get();
    }

    @Override
    public String name() {
        return "Animate";
    }

    @Override
    public void execute() {
        if (explosionAnimation.get() != null) {
            explosionAnimation.get().stop();
            explosionAnimation.set(null);
        }

        if (pulseAnimation.get() != null) {
            pulseAnimation.get().stop();
            pulseAnimation.set(null);
        }
    }

    @Override
    public void undo() {
        if (initialExplosion != null) {
            explosionAnimation.set(initalPulse);
            initialExplosion.start();
        }

        if (pulseAnimation.get() != null) {
            pulseAnimation.set(null);
            initalPulse.start();
        }
    }
}
