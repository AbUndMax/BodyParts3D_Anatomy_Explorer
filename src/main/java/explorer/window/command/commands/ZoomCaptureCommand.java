package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.MyCamera;


/**
 * Command to apply a saved zoom operation to the explorer window's camera.
 * Provides undo functionality to revert the camera to its previous zoom position.
 *
 * The Command captures the initial and final zoom positions
 * during a drag-based zoom interaction and encapsulates the whole interaction in one undoable command.
 */
public class ZoomCaptureCommand implements Command {

    private final MyCamera camera;
    private final double zoomStart;
    private final double zoomEnd;

    /**
     * Constructs a ZoomCaptureCommand with the specified camera and the initial and final zoom positions.
     *
     * @param camera the camera to be zoomed
     * @param zoomStart the starting zoom position of the camera
     * @param zoomEnd the ending zoom position of the camera
     */
    public ZoomCaptureCommand(MyCamera camera, double zoomStart, double zoomEnd) {
        this.camera = camera;
        this.zoomStart = zoomStart;
        this.zoomEnd = zoomEnd;
    }

    /**
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Zoom";
    }

    /**
     * Executes the command by applying the final zoom position to the camera.
     */
    @Override
    public void execute() {
        camera.setTranslateZ(zoomEnd);
    }

    /**
     * Reverts the camera's zoom to its initial position, effectively undoing the zoom action.
     */
    @Override
    public void undo() {
        camera.setTranslateZ(zoomStart);
    }
}
