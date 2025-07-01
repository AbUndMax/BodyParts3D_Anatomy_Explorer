package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.MyCamera;

public class ZoomCommand implements Command {

    private final MyCamera camera;
    private final double zoom;

    public ZoomCommand(MyCamera camera, double zoom) {
        this.camera = camera;
        this.zoom = zoom;
    }

    @Override
    public String name() {
        return "Zoom";
    }

    @Override
    public void execute() {
        camera.zoom(zoom);
    }

    @Override
    public void undo() {
        camera.zoom(-zoom);
    }
}
