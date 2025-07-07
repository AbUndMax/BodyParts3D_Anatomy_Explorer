
package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.MyCamera;

/**
 * Command to translate (pan) the explorer window's camera by a specified distance along the x and y axes.
 * Provides undo functionality to reverse the translation movement.
 */
public class TranslateCommand implements Command {

    private final MyCamera myCamera;
    private final double x;
    private final double y;

    /**
     * Constructs a TranslateCommand with the specified camera and translation distances.
     *
     * @param camera the camera to be translated
     * @param x the distance to pan along the x-axis
     * @param y the distance to pan along the y-axis
     */
    public TranslateCommand(MyCamera camera, double x, double y) {
        this.myCamera = camera;
        this.x = x;
        this.y = y;
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Translation";
    }

    /**
     * Executes the command by panning the camera by the specified x and y distances.
     */
    @Override
    public void execute() {
        myCamera.pan(x, y);
    }

    /**
     * Reverses the translation by panning the camera in the opposite direction of the initial movement.
     */
    @Override
    public void undo() {
        myCamera.pan(-x, -y);
    }
}
