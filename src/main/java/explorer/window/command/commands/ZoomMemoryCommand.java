package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.MyCamera;

public class ZoomMemoryCommand implements Command {

    private final MyCamera camera;
    private final double zoomStart;
    private final double zoomEnd;

    public ZoomMemoryCommand(MyCamera camera, double zoomStart, double zoomEnd) {
        this.camera = camera;
        this.zoomStart = zoomStart;
        this.zoomEnd = zoomEnd;
    }

    @Override
    public String name() {
        return "Zoom";
    }

    @Override
    public void execute() {
        camera.setTranslateZ(zoomEnd);
    }

    @Override
    public void undo() {
        camera.setTranslateZ(zoomStart);
    }
}
