package explorer.window.command;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandManager {

    private static final int MAX_REDO = 100;

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    private final ObjectProperty<Command> lastRedoCommand = new SimpleObjectProperty<>();
    private final ObjectProperty<Command> lastUndoCommand = new SimpleObjectProperty<>();

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

    public boolean canUndo(){
        return !undoStack.isEmpty();
    }

    public boolean canRedo(){
        return !redoStack.isEmpty();
    }

    public void undo(){
        if (canUndo()) {
            Command cmd = undoStack.pop();
            lastUndoCommand.set(undoStack.peek());
            cmd.undo();
            redoStack.push(cmd);
            lastRedoCommand.set(cmd);
        }
    }

    public void redo(){
        if (canRedo()) {
            Command cmd = redoStack.pop();
            lastRedoCommand.set(redoStack.peek());
            cmd.execute();
            undoStack.push(cmd);
            lastUndoCommand.set(cmd);
        }
    }

    public ObjectProperty<Command> getLastUndoCommand() {
        return lastUndoCommand;
    }

    public ObjectProperty<Command> getLastRedoCommand() {
        return lastRedoCommand;
    }
}
