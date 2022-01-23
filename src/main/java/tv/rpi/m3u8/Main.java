package tv.rpi.m3u8;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.*;
import tv.rpi.m3u8.control.MainWindowController;
import tv.rpi.m3u8.model.common.DownloaderSettings;
import tv.rpi.m3u8.model.common.OperatingSystem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main extends Application {

    public static final String GITHUB_URL = "https://github.com/rpitv/m3u8-downloader";
    public static final Logger LOGGER = LogManager.getLogger("tv.rpi.m3u8");
    public static final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
    public static final List<Process> processList = new ArrayList<>();

    private static final String settingsFilePath = ".rpitv/settings";
    private static DownloaderSettings settings;
    static {
        try {
            FileInputStream fileIn = new FileInputStream(Main.settingsFilePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Main.settings = (DownloaderSettings) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            Main.LOGGER.warn("Failed to read settings file.");
            Main.logThrowable(e);
            Main.settings = new DownloaderSettings(Main.settingsFilePath);
        }
    }

    public static String errorMessageToDisplay = null;
    public static MainWindowController mainWindowController = null;
    public static Stage mainStage = null;

    public static DownloaderSettings getSettings() {
        if(Main.settings == null) {
            Main.settings = new DownloaderSettings(Main.settingsFilePath);
        }
        return Main.settings;
    }

    public static void quit() {
        try {
            Main.settings.save();
        } catch (IOException e) {
            Main.LOGGER.error("Unable to save settings file!");
            Main.logThrowable(e);
        }
        for(final Process p : Main.processList) {
            if(p.isAlive()) {
                p.destroy();
            }
        }
        Platform.exit();
        Main.threadPool.shutdownNow();
        System.exit(0);
    }

    private static void enableVerboseLogging() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final LoggerConfig loggerConfig = config.getLoggers().get("tv.rpi.m3u8");
        loggerConfig.removeAppender("Console");
        loggerConfig.removeAppender("File");
        loggerConfig.addAppender(config.getAppender("Console"), Level.ALL, null);
        loggerConfig.addAppender(config.getAppender("VerboseFile"), Level.ALL, null);
        ctx.updateLoggers();
    }

    public static void logThrowable(Throwable t) {
        LOGGER.atTrace().withThrowable(t).log("Stack trace:");
    }

    public static OperatingSystem getOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.contains("windows")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("mac")) {
            return OperatingSystem.MAC;
        } else if (osName.contains("linux")) {
            return OperatingSystem.LINUX;
        } else {
            return OperatingSystem.OTHER;
        }
    }

    public static void main(String[] args) {
        if (Boolean.getBoolean("verbose")) {
            Main.enableVerboseLogging();
        }

        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.mainStage = primaryStage;
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/mainWindow.fxml"));
        final Parent root = loader.load();
        Font f = Font.loadFont(getClass().getResourceAsStream("/fonts/fonts.css"), 10);
        //noinspection ConstantConditions
        root.getStylesheets().add(getClass().getResource("/fonts/fonts.css").toExternalForm());
        root.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());

        Platform.runLater(root::requestFocus);

        final Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest((event) -> {
            Main.quit();
        });

        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setTitle(".m3u8 Downloader");
        primaryStage.getIcons().add(new Image("/images/logo.png"));
        // Red: #B73535
        // Grey: #A7A7A7

        primaryStage.show();
    }
}
