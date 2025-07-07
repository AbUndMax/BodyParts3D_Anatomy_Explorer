package explorer.model;

import javafx.stage.DirectoryChooser;
import java.io.File;

/**
 * Class for Obj I/O actions
 */
public class ObjIO {

    /**
     * opens Directory Chooser for selecting the appropriate .Obj file folder.
     * @return the path to the chosen folder
     */
    public static File openDirectoryChooser() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder containing .obj files");
        return chooser.showDialog(null);
    }
}

