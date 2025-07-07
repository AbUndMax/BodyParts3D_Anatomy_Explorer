package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.MyCamera;

/**
 * Command to apply a saved translation (pan) to the explorer window's camera.
 * Provides undo functionality to revert the camera to its previous position.
 *
 * The Command captures the initial and final camera positions
 * during a drag-based translation and encapsulates the whole interaction in one undoable command.
 */
public class TranslateCaptureCommand implements Command {

    private final MyCamera myCamera;
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;

    /**
     * Constructs a TranslateMemoryCommand with the target camera and its start and end translation positions.
     *
     * @param camera the camera to be translated
     * @param startX the starting X position of the camera
     * @param startY the starting Y position of the camera
     * @param endX the ending X position of the camera
     * @param endY the ending Y position of the camera
     */
    public TranslateCaptureCommand(MyCamera camera, double startX, double startY, double endX, double endY) {
        this.myCamera = camera;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
    public String name() {
        return "Translation";
    }

    /**
     * Executes the command by moving the camera to the specified end position.
     */
    @Override
    public void execute() {
        myCamera.setTranslateX(endX);
        myCamera.setTranslateY(endY);
    }

    /**
     * Reverses the translation by moving the camera back to its original start position.
     */
    @Override
    public void undo() {
        myCamera.setTranslateX(startX);
        myCamera.setTranslateY(startY);
    }
}
