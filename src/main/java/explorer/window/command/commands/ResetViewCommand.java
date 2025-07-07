package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.presenter.VisualizationViewPresenter;
import explorer.window.vistools.MyCamera;
import javafx.scene.Group;
import javafx.scene.transform.Affine;


/**
 * Command to reset the explorer window view to its initial transform and camera position.
 * Provides undo functionality to restore the previous view and camera settings.
 */
public class ResetViewCommand implements Command {

    private final Group contentGroup;
    private final Affine beforeReset;
    private final MyCamera camera;
    private final double cameraX;
    private final double cameraY;
    private final double cameraZ;

    /**
     * Constructs a ResetViewCommand that captures the current transform and camera position.
     *
     * @param contentGroup the group whose view transform will be reset
     * @param camera the camera to be reset
     */
    public ResetViewCommand(Group contentGroup, MyCamera camera) {
        this.contentGroup = contentGroup;

        // Capture the current Affine transform
        this.beforeReset = new Affine(contentGroup.getTransforms().getFirst());

        this.camera = camera;
        this.cameraX = camera.getTranslateX();
        this.cameraY = camera.getTranslateY();
        this.cameraZ = camera.getTranslateZ();
    }

    @Override
    /**
     * @return the name of the command
     */
    public String name() {
        return "Reset View";
    }

    @Override
    /**
     * Executes the command by resetting the camera view and content group transform to their initial states.
     */
    public void execute() {
        camera.resetView();
        contentGroup.getTransforms().setAll(VisualizationViewPresenter.INITIAL_TRANSFORM);
    }

    @Override
    /**
     * Undoes the reset of the transforms and camera reset,
     * by restoring the previous camera position and content group transform.
     */
    public void undo() {
        // Restore view transform
        contentGroup.getTransforms().setAll(beforeReset);
        // Restore camera position
        camera.setTranslateX(cameraX);
        camera.setTranslateY(cameraY);
        camera.setTranslateZ(cameraZ);
    }
}
