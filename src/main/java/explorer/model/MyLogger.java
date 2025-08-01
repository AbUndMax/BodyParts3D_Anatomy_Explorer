package explorer.model;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {
    private static final Path logDir = Paths.get(System.getProperty("user.home"), ".anatomyExplorer", "logs");
    private static final Logger logger = Logger.getLogger(MyLogger.class.getName());

    static {
        try {
            redirectSystemOutAndErr();
            Files.createDirectories(logDir);

            Path currentLog = logDir.resolve("app_current.log");
            Path previousLog = logDir.resolve("app_previous.log");

            // log rotation: delete previous log, replace currentLog into previousLog and use currentLog in this session
            if (Files.exists(currentLog)) {
                Files.deleteIfExists(previousLog);
                Files.move(currentLog, previousLog, StandardCopyOption.REPLACE_EXISTING);
            }


            FileHandler fh = new FileHandler(currentLog.toString(), false);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    private static void redirectSystemOutAndErr() {
        // Originalstreams sichern
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        // System.out umleiten
        System.setOut(new PrintStream(originalOut) {
            @Override
            public void println(String x) {
                // In Konsole
                originalOut.println(x);
                // In Logger
                MyLogger.getLogger().info(x);
            }
        });

        // System.err umleiten
        System.setErr(new PrintStream(originalErr) {
            @Override
            public void println(String x) {
                // In Konsole
                originalErr.println(x);
                // In Logger
                MyLogger.getLogger().severe(x);
            }
        });
    }
}
