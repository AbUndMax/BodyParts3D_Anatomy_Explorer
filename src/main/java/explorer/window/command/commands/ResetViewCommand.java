package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.presenter.VisualizationViewPresenter;
import explorer.window.vistools.MyCamera;
import javafx.scene.Group;
import javafx.scene.transform.Affine;

public class ResetViewCommand implements Command {

    private final Group contentGroup;
    private final Affine beforeReset;
    private final MyCamera camera;
    private final double cameraX;
    private final double cameraY;
    private final double cameraZ;

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
    public String name() {
        return "Reset View";
    }

    @Override
    public void execute() {
        camera.resetView();
        contentGroup.getTransforms().setAll(VisualizationViewPresenter.INITIAL_TRANSFORM);
    }

    @Override
    public void undo() {
        // Restore view transform
        contentGroup.getTransforms().setAll(beforeReset);
        // Restore camera position
        camera.setTranslateX(cameraX);
        camera.setTranslateY(cameraY);
        camera.setTranslateZ(cameraZ);
    }
}
