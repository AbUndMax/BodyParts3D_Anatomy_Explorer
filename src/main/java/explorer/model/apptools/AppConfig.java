package explorer.model.apptools;

import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;


/**
 * Handles application-specific configuration using Java's Preferences API.
 * This configuration node stores and retrieves simple key-value pairs, such as the last used file path.
 * Uses the AppConfig class as the preference node.
 */
public class AppConfig {
    private static final Preferences PREFS = Preferences.userNodeForPackage(AppConfig.class);
    private static final String KEY_LAST_PATH = "path";

    /**
     * Saves the provided file path as the last used path in the preferences.
     *
     * @param path the file path to save
     */
    public static void saveLastPath(String path) {
        PREFS.put(KEY_LAST_PATH, path);
        try {
            PREFS.flush();
        } catch (BackingStoreException e) {
            AppLogger.getLogger().log(Level.SEVERE, "Couldn't save the .obj Folder path", e);
        }
    }

    /**
     * Loads the last saved file path from the preferences.
     *
     * @return the last saved file path, or an empty string if not set
     */
    public static String loadLastPath() {
        return PREFS.get(KEY_LAST_PATH, "");
    }

    /**
     * Clears the last saved file path by saving an empty string.
     */
    public static void invalidateLastPath() {
        saveLastPath("");
    }
}