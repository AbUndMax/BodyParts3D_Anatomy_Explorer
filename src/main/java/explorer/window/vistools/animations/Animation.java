package explorer.window.vistools.animations;

public interface Animation {

    /**
     * starts the animation
     */
    public void start();

    /**
     * stops the animation but also returns to the state BEFORE the animation
     */
    public void reset();

    /**
     * stops the animation with a "stop" animation -> simple stop.
     */
    public void stop();

    /**
     * Returns run state of the animation
     * @return true if animation is running, else false
     */
    public boolean isRunning();
}
