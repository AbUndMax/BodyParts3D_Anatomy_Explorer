package explorer.window.command;

public interface Command {
    public String name();

    public void execute();

    public void undo();
}
