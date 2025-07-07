package explorer.window.command;

/**
 * Represents a command that can be executed and undone.
 */
public interface Command {

    /**
     * Returns the name of the command.
     * @return the name of the command
     */
    public String name();

    /**
     * Executes the action defined by the command.
     */
    public void execute();

    /**
     * Reverses the action performed by the command.
     */
    public void undo();
}
