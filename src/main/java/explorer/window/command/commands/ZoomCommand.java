package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.MyCamera;


/**
 * Command to apply a zoom operation to the explorer window's camera.
 * Provides undo functionality to reverse the applied zoom.
 */
public class ZoomCommand implements Command {

    private final MyCamera camera;
    private final double zoom;

    /**
     * Constructs a ZoomCommand with the specified camera and zoom factor.
     *
     * @param camera the camera to be zoomed
     * @param zoom the zoom factor to apply (positive for zoom in, negative for zoom out)
     */
    public ZoomCommand(MyCamera camera, double zoom) {
        this.camera = camera;
        this.zoom = zoom;
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Zoom";
    }

    /**
     * Executes the command by applying the zoom factor to the camera.
     */
    @Override
    public void execute() {
        camera.zoom(zoom);
    }

    /**
     * Reverses the zoom operation by applying the inverse of the original zoom factor.
     */
    @Override
    public void undo() {
        camera.zoom(-zoom);
    }
}
