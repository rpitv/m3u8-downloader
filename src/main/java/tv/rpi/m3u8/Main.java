package tv.rpi.m3u8;

import javafx.application.Application;
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
import tv.rpi.m3u8.control.MainWindow;
import tv.rpi.m3u8.model.common.OperatingSystem;

import java.util.Locale;

public class Main extends Application {

    public static final String GITHUB_URL = "https://github.com/rpitv/m3u8-downloader";
    public static final Logger LOGGER = LogManager.getLogger("tv.rpi.m3u8");
    public static String errorMessageToDisplay = null;
    public static MainWindow mainWindow = null;

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
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/mainWindow.fxml"));
        final Parent root = loader.load();
        Font f = Font.loadFont(getClass().getResourceAsStream("/fonts/fonts.css"), 10);
        //noinspection ConstantConditions
        root.getStylesheets().add(getClass().getResource("/fonts/fonts.css").toExternalForm());

        final Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setTitle(".m3u8 Downloader");
        primaryStage.getIcons().add(new Image("/images/logo.png"));
        // Red: #B73535
        // Grey: #A7A7A7

        primaryStage.show();
    }
}
