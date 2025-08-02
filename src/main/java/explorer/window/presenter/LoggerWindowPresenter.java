package explorer.window.presenter;

import explorer.model.IO;
import explorer.model.MyLogger;
import explorer.window.controller.LoggerWindowController;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;
import java.util.logging.Level;

public class LoggerWindowPresenter {

    LoggerWindowController controller;

    public LoggerWindowPresenter(LoggerWindowController controller) {
        this.controller = controller;

        setupTextArea();
        setupButtons();
    }

    private void setupButtons() {
        Button exportButton = controller.getExportLogButton();
        Button clearButton = controller.getClearLogButton();

        exportButton.setOnAction(event -> {
            IO.exportLogger((Stage) exportButton.getScene().getWindow());
        });

        clearButton.setOnAction(event -> {
            try {
                Files.writeString(MyLogger.getCurrentLogPath(), "");
                MyLogger.getLogger().log(Level.INFO, "Log file cleared");
            } catch (IOException e) {
                MyLogger.getLogger().log(Level.SEVERE, "Failed to clear log file", e);
            }
        });
    }

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

    private record LogWatcher(Path logFile, Runnable onChange) implements Runnable {

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
