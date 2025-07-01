package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.MyCamera;

public class TranslateCommand implements Command {

    private final MyCamera myCamera;
    private final double x;
    private final double y;

    public TranslateCommand(MyCamera camera, double x, double y) {
        this.myCamera = camera;
        this.x = x;
        this.y = y;
    }

    @Override
    public String name() {
        return "Translation";
    }

    @Override
    public void execute() {
        myCamera.pan(x, y);
    }

    @Override
    public void undo() {
        myCamera.pan(-x, -y);
    }
}
