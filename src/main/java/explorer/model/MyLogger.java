package explorer.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Application-wide logging utility for the Anatomy Explorer.
 * <p>
 * This class sets up a global {@link Logger} instance that writes all log messages
 * to a dedicated log directory within the user's home folder:
 * <pre>
 *   ~/.anatomyExplorer/logs
 * </pre>
 * <p>
 * Log rotation is performed at startup: the existing current log file is moved
 * to a "previous" log file before a new current log is created for the active session.
 * </p>
 * <p>
 * The logger is configured to capture messages at all levels ({@link Level#ALL})
 * and formats them using {@link SimpleFormatter}.
 * </p>
 */
public class MyLogger {

    /** Path to the directory where log files are stored. */
    private static final Path logDir = Paths.get(System.getProperty("user.home"), ".anatomyExplorer", "logs");

    /** The singleton {@link Logger} instance for the application. */
    private static final Logger logger = Logger.getLogger(MyLogger.class.getName());

    /** Path to the current log file for this session. */
    private static Path currentLog = null;

    /** Path to the log file for the previous session. */
    private static Path previousLog = null;

    static {
        try {
            // Ensure log directory exists
            Files.createDirectories(logDir);

            currentLog = logDir.resolve("app_current.log");
            previousLog = logDir.resolve("app_previous.log");

            // Perform log rotation: move current log to previous, if it exists
            if (Files.exists(currentLog)) {
                Files.deleteIfExists(previousLog);
                Files.move(currentLog, previousLog, StandardCopyOption.REPLACE_EXISTING);
            }

            // Create a file handler to write log messages to the current log file
            FileHandler fh = new FileHandler(currentLog.toString(), false);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the singleton application {@link Logger} instance.
     *
     * @return the global application logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Returns the path to the current session's log file.
     *
     * @return {@link Path} to the active log file
     */
    public static Path getCurrentLogPath() {
        return currentLog;
    }

    /**
     * Returns the path to the previous session's log file.
     *
     * @return {@link Path} to the active log file
     */
    public static Path getPreviousLogPath() {
        return previousLog;
    }
}
