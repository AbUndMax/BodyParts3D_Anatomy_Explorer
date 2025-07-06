package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.animations.Animation;

import java.util.concurrent.atomic.AtomicReference;

public class ClearAnimationCommand implements Command {

    private final AtomicReference<Animation> explosionAnimation;
    public final Animation initialExplosion;
    private final AtomicReference<Animation> pulseAnimation;
    private final Animation initalPulse;
    private final AtomicReference<Animation> contRotateAnimation;
    private final Animation initContRotate;

    public ClearAnimationCommand(AtomicReference<Animation> explosionAnimation, AtomicReference<Animation> pulseAnimation, AtomicReference<Animation> contRotateAnimation) {
        this.explosionAnimation = explosionAnimation;
        initialExplosion = explosionAnimation.get();
        this.pulseAnimation = pulseAnimation;
        initalPulse = pulseAnimation.get();
        this.contRotateAnimation = contRotateAnimation;
        initContRotate = contRotateAnimation.get();
    }

    @Override
    public String name() {
        return "Animate";
    }

    @Override
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
    public void undo() {
        if (explosionAnimation.get() == null && initialExplosion != null) {
            explosionAnimation.set(initialExplosion);
            explosionAnimation.get().start();
        }

        if (pulseAnimation.get() == null && initalPulse != null) {
            pulseAnimation.set(initalPulse);
            pulseAnimation.get().start();
        }

        if (contRotateAnimation.get() == null && initContRotate != null) {
            contRotateAnimation.set(initContRotate);
            contRotateAnimation.get().start();
        }
    }
}
