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
 * Class for Obj I/O actions
 */
public class IO {

    /**
     * opens Directory Chooser for selecting the appropriate .Obj file folder.
     * @return the path to the chosen folder
     */
    public static File openDirectoryChooser() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder containing .obj files");
        return chooser.showDialog(null);
    }

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

