package tv.rpi.m3u8.control;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.apache.commons.compress.archivers.ArchiveException;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.common.AbstractFfmpegHandler;

import java.io.IOException;

public class Installing {

    @FXML
    public Label label;
    @FXML
    public ProgressBar progress;

    public void initialize() {
        progress.setProgress(0);
        final Object lock = new Object();
        new Thread(() -> {
            AbstractFfmpegHandler ffmpeg = AbstractFfmpegHandler.getForOperatingSystem(Main.getOperatingSystem());
            try {
                ffmpeg.install((feedback, currentStep) -> {
                    synchronized (lock) {
                        Platform.runLater(() -> {
                            progress.setProgress(feedback);
                            label.setText(currentStep);
                        });
                    }
                });
            } catch (IOException | ArchiveException e) {
                Main.LOGGER.fatal("Failed to install ffmpeg.");
                Main.logThrowable(e);
                Main.errorMessageToDisplay = "Failed to install ffmpeg.";
                Main.mainWindow.swapScenes("/scenes/error.fxml");
            }
        }).start();
    }
}
