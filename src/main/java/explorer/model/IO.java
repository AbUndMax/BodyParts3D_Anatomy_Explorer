package explorer.model;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * Utility class providing input/output related operations for the Explorer application.
 * <p>
 * This includes methods for user-driven file/folder selection dialogs,
 * as well as export functionality for the application log file.
 * </p>
 */
public class IO {

    /**
     * Opens a directory chooser dialog allowing the user to select
     * a folder containing the desired <code>.obj</code> files.
     *
     * @return the {@link File} object representing the chosen folder,
     *         or {@code null} if the user cancels the dialog
     */
    public static File openDirectoryChooser() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder containing .obj files");
        return chooser.showDialog(null);
    }

    /**
     * Opens a save dialog to export the current application log file
     * to a location selected by the user.
     * <p>
     * The file chooser is configured to suggest a <code>.log</code> extension
     * and pre-fills the current log file's name as the default filename.
     * If the user selects a target file, the current log file is copied there.
     * </p>
     *
     * @param owner the owner {@link Window} for the file chooser dialog,
     *              ensuring it appears in front of the main application window
     */
    public static void exportLogger(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Log File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Log Files", "*.log"));
        fileChooser.setInitialFileName(MyLogger.getCurrentLogPath().getFileName().toString());

        File selectedFile = fileChooser.showSaveDialog(owner);
        if (selectedFile != null) {
            try {
                Files.copy(MyLogger.getCurrentLogPath(), selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                MyLogger.getLogger().log(Level.INFO, "Log file exported to: " + selectedFile.getAbsolutePath());
            } catch (IOException e) {
                MyLogger.getLogger().log(Level.SEVERE, "Failed to export log file", e);
            }
        }
    }
}

