package explorer.model;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

public class AppConfig {
    private static final Preferences PREFS = Preferences.userNodeForPackage(AppConfig.class);
    private static final String KEY_LAST_PATH = "path";

    public static void saveLastPath(String path) {
        PREFS.put(KEY_LAST_PATH, path);
        try {
            PREFS.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static String loadLastPath() {
        return PREFS.get(KEY_LAST_PATH, "");
    }
}