package explorer.window.presenter;

import explorer.model.IO;
import explorer.model.MyLogger;
import explorer.window.controller.LoggerWindowController;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.nio.file.*;
import java.util.logging.Level;

/**
 * Creates a new presenter for the logger window.
 * <p>
 * Initializes the text area to display log file content and
 * configures the export/clear log buttons.
 * </p>
 */
public class LoggerWindowPresenter {

    LoggerWindowController controller;

    public LoggerWindowPresenter(LoggerWindowController controller) {
        this.controller = controller;

        setupTextArea();
        setupButtons();
    }

    /**
     * Configures the action handlers for the export and clear log buttons.
     * <p>
     * The export button opens a {@link javafx.stage.FileChooser} dialog to allow
     * the user to select a location to save a copy of the current log file. The clear
     * button overwrites the log file with an empty string, effectively clearing it,
     * and logs this action as an informational message.
     * </p>
     */
    private void setupButtons() {
        SplitMenuButton exportButton = controller.getExportLogButton();
        MenuItem exportPrevious = controller.getExportPreviousSessionMenuItem();
        Button clearButton = controller.getClearLogButton();

        // Export log file to user-selected location
        exportButton.setOnAction(event -> {
            Path currentPath = MyLogger.getCurrentLogPath();
            if (currentPath != null) {
                IO.exportLogger(exportButton.getScene().getWindow(), currentPath);
            }
        });

        // Export log file to user-selected location for previous session
        exportPrevious.setOnAction(event -> {
            Path previousPath = MyLogger.getPreviousLogPath();
            if (previousPath != null) {
                IO.exportLogger(exportButton.getScene().getWindow(), previousPath);
            }
        });

        // Clear the contents of the current log file
        clearButton.setOnAction(event -> {
            try {
                Files.writeString(MyLogger.getCurrentLogPath(), "");
                MyLogger.getLogger().log(Level.INFO, "Log file cleared");
            } catch (IOException e) {
                MyLogger.getLogger().log(Level.SEVERE, "Failed to clear log file", e);
            }
        });
    }

    /**
     * Initializes the log display text area and sets up a background watcher thread.
     * <p>
     * Reads the initial content of the log file and displays it in the UI,
     * then starts a daemon thread that monitors the log file for modifications.
     * Whenever the log file changes, the UI is updated with the latest content.
     * </p>
     */
    private void setupTextArea() {
        TextArea logOut = controller.getLogTextArea();

        Runnable updateLog = () -> {
            try {
                String content = Files.readString(MyLogger.getCurrentLogPath());
                javafx.application.Platform.runLater(() -> logOut.setText(content));
            } catch (IOException e) {
                MyLogger.getLogger().log(Level.WARNING, "Couldn't set LogWatcher", e);
            }
        };

        updateLog.run();

        Thread watcherThread = new Thread(new LogWatcher(MyLogger.getCurrentLogPath(), updateLog));
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    /**
     * Watches the specified log file for modifications and triggers
     * a provided callback when changes are detected.
     * <p>
     * This record is used internally to keep the logger window UI in sync
     * with the contents of the log file in real-time.
     * </p>
     *
     * @param logFile  the {@link Path} to the log file being monitored
     * @param onChange a {@link Runnable} callback executed whenever the file changes
     */
    private record LogWatcher(Path logFile, Runnable onChange) implements Runnable {

        /**
         * Continuously monitors the directory containing the log file for changes.
         * When the log file is modified, invokes the {@code onChange} callback.
         * The watch loop runs until interrupted.
         */
        @Override
        public void run() {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                logFile.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                // the following while loop was created with the help of AI
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        if (changed.endsWith(logFile.getFileName())) {
                            onChange.run();
                        }
                    }
                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                MyLogger.getLogger().log(Level.WARNING, "Couldn't run LogWatcher", e);
            }
        }
    }

}
