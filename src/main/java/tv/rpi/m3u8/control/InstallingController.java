package tv.rpi.m3u8.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.apache.commons.compress.archivers.ArchiveException;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.DownloaderScenes;
import tv.rpi.m3u8.model.common.AbstractFfmpegHandler;

import java.io.IOException;

public class InstallingController {

    @FXML
    public Label label;
    @FXML
    public ProgressBar progress;
    public Button continueButton;

    public void initialize() {
        progress.setProgress(0);
        continueButton.setVisible(false);
        Main.threadPool.submit(() -> {
            final AbstractFfmpegHandler ffmpeg = AbstractFfmpegHandler.getForOperatingSystem(Main.getOperatingSystem());
            try {
                ffmpeg.install((feedback, currentStep) -> {
                    Platform.runLater(() -> {
                        this.progress.setProgress(feedback);
                        this.label.setText(currentStep);
                    });
                });
            } catch (IOException | ArchiveException e) {
                Main.LOGGER.fatal("Failed to install ffmpeg.");
                Main.logThrowable(e);
                Main.errorMessageToDisplay = "Failed to install ffmpeg.";
                Main.mainWindowController.swapScenes(DownloaderScenes.ERROR);
            }
            Platform.runLater(() -> {
                this.continueButton.setVisible(true);
            });
        });
    }

    public void doContinue(ActionEvent event) {
        Main.mainWindowController.swapScenes(DownloaderScenes.DOWNLOAD_INPUT);
    }
}
