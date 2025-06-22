package explorer.model;

import javafx.stage.DirectoryChooser;
import java.io.File;
import java.util.function.Consumer;

/**
 * Class completely copied from assignment06 with smaller refactors
 */
public class ObjIO {

    public static File openDirectoryChooser() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder containing .obj files");
        return chooser.showDialog(null);
    }
}

