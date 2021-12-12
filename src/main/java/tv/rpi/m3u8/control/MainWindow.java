package tv.rpi.m3u8.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.common.AbstractFfmpegHandler;
import tv.rpi.m3u8.model.common.OperatingSystem;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

public class MainWindow {

    @FXML
    public Label copyright;
    @FXML
    public Pane content;

    public void swapScenes(String path) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            final Parent root = loader.load();
            content.getChildren().clear();
            content.getChildren().addAll(root.getChildrenUnmodifiable());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        Main.mainWindow = this;
        copyright.setText("RPI TV © " + Calendar.getInstance().get(Calendar.YEAR));

        this.swapScenes("/scenes/loading-spinner.fxml");

        final OperatingSystem os = Main.getOperatingSystem();
        final AbstractFfmpegHandler ffmpeg = AbstractFfmpegHandler.getForOperatingSystem(os);
        try {
            if(!ffmpeg.isFfmpegInstalled()) {
                Main.LOGGER.warn("ffmpeg is not installed!");
                this.swapScenes("/scenes/noFFmpegInstallation.fxml");
            } else {
                this.swapScenes("/scenes/downloadCollection.fxml");
            }
        } catch (IOException | InterruptedException e) {
            Main.LOGGER.fatal("Failed to locate ffmpeg.");
            Main.logThrowable(e);
            Main.errorMessageToDisplay = "Failed to locate ffmpeg.";
            this.swapScenes("/scenes/error.fxml");
        }
    }

    @FXML
    private void openRepo(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI(Main.GITHUB_URL));
        } catch (IOException | URISyntaxException e) {
            Main.LOGGER.error("An unexpected exception occurred while trying to open the GitHub repository.");
            Main.logThrowable(e);
        }
    }
}
