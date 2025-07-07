package explorer.window.command;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages the execution, undo, and redo of commands.
 * Maintains a history of executed commands with undo and redo functionality.
 * Limits the undo stack to a maximum size to prevent unbounded memory growth.
 * Does vary from the proposed CommandManager in the lecture - but follows same idea!
 */
public class CommandManager {

    private static final int MAX_REDO = 100;

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    private final ObjectProperty<Command> lastRedoCommand = new SimpleObjectProperty<>();
    private final ObjectProperty<Command> lastUndoCommand = new SimpleObjectProperty<>();

    /**
     * Executes a command and adds it to the undo stack.
     * Clears the redo stack to maintain a linear command history.
     * Limits the size of the undo stack to MAX_REDO.
     *
     * @param cmd the command to execute
     */
    public void executeCommand(Command cmd){
        cmd.execute();
        undoStack.push(cmd);
        lastUndoCommand.set(cmd);

        // clear redo to maintain linear command history
        redoStack.clear();

        // limit undo history to specific value to prevent endless stack growing
        if (undoStack.size() > MAX_REDO) {
            undoStack.removeLast();
        }
    }

    /**
     * Checks if there is a command available to undo.
     *
     * @return true if undo is possible, false otherwise
     */
    public boolean canUndo(){
        return !undoStack.isEmpty();
    }

    /**
     * Checks if there is a command available to redo.
     *
     * @return true if redo is possible, false otherwise
     */
    public boolean canRedo(){
        return !redoStack.isEmpty();
    }

    /**
     * Undoes the last executed command and pushes it onto the redo stack.
     * Updates the last undo and redo command properties.
     */
    public void undo(){
        if (canUndo()) {
            Command cmd = undoStack.pop();
            lastUndoCommand.set(undoStack.peek());
            cmd.undo();
            redoStack.push(cmd);
            lastRedoCommand.set(cmd);
        }
    }

    /**
     * Redoes the last undone command and pushes it back onto the undo stack.
     * Updates the last undo and redo command properties.
     */
    public void redo(){
        if (canRedo()) {
            Command cmd = redoStack.pop();
            lastRedoCommand.set(redoStack.peek());
            cmd.execute();
            undoStack.push(cmd);
            lastUndoCommand.set(cmd);
        }
    }

    /**
     * Gets the property holding the last undo command.
     *
     * @return the property of the last undo command
     */
    public ObjectProperty<Command> getLastUndoCommand() {
        return lastUndoCommand;
    }

    /**
     * Gets the property holding the last redo command.
     *
     * @return the property of the last redo command
     */
    public ObjectProperty<Command> getLastRedoCommand() {
        return lastRedoCommand;
    }
}
