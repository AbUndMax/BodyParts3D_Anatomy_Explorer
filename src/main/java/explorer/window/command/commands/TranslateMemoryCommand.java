package explorer.window.command.commands;

import explorer.window.command.Command;
import explorer.window.vistools.MyCamera;

public class TranslateMemoryCommand implements Command {

    private final MyCamera myCamera;
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;

    public TranslateMemoryCommand(MyCamera camera, double startX, double startY, double endX, double endY) {
        this.myCamera = camera;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public String name() {
        return "Translation";
    }

    @Override
    public void execute() {
        myCamera.setTranslateX(endX);
        myCamera.setTranslateY(endY);
    }

    @Override
    public void undo() {
        myCamera.setTranslateX(startX);
        myCamera.setTranslateY(startY);
    }
}
