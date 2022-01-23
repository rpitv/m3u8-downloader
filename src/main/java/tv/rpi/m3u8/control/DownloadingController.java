package tv.rpi.m3u8.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.DownloaderScenes;
import tv.rpi.m3u8.model.common.AbstractFfmpegHandler;
import tv.rpi.m3u8.model.common.DownloaderSettings;

import java.io.IOException;

public class DownloadingController {
    @FXML
    public Label label;
    @FXML
    public ProgressBar progress;
    @FXML
    public Button goBackButton;

    public void initialize() {
        progress.setProgress(0);
        goBackButton.setVisible(false);

        Main.threadPool.submit(() -> {
            final AbstractFfmpegHandler ffmpeg = AbstractFfmpegHandler.getForOperatingSystem(Main.getOperatingSystem());
            try {
                if(!ffmpeg.isFfmpegInstalled()) {
                    Main.mainWindowController.swapScenes(DownloaderScenes.NO_FFMPEG_INSTALLATION);
                }

                final DownloaderSettings settings = Main.getSettings();
                ffmpeg.download(settings.lastUsedM3u8, settings.lastUsedOutputDir, (int) settings.compressionValue, ((percentage, details) -> {
                    Platform.runLater(() -> {
                        this.progress.setProgress(percentage);
                        this.label.setText(details);
                    });
                }));
            } catch (IOException | InterruptedException e) {
                Main.LOGGER.error("Attempted to initialize an ffmpeg download but attempt to find ffmpeg installation failed!");
                Main.logThrowable(e);
                Main.errorMessageToDisplay = "ffmpeg installation couldn't be found.";
                Main.mainWindowController.swapScenes(DownloaderScenes.ERROR);
                return;
            }
            Platform.runLater(() -> {
                this.goBackButton.setVisible(true);
            });
        });
    }

    public void goBack(ActionEvent actionEvent) {
        Main.mainWindowController.swapScenes(DownloaderScenes.DOWNLOAD_INPUT);
    }
}
