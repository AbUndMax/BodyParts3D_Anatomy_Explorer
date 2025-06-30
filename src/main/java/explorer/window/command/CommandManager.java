package explorer.window.command;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandManager {

    private static final int MAX_REDO = 20;

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    private final ObjectProperty<Command> lastRedoCommand = new SimpleObjectProperty<>();
    private final ObjectProperty<Command> lastUndoCommand = new SimpleObjectProperty<>();

    public void executeCommand(Command cmd){
        cmd.execute();
        undoStack.push(cmd);
        lastUndoCommand.set(cmd);

        // limit redo history to specific value to prevent endless stack growing
        if (redoStack.size() > MAX_REDO) {
            redoStack.removeLast();
        }
    }

    public void undo(){
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.pop();
            lastUndoCommand.set(undoStack.peek());
            cmd.undo();
            redoStack.push(cmd);
            lastRedoCommand.set(cmd);
        }
    }

    public void redo(){
        if (!redoStack.isEmpty()) {
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
