package tv.rpi.m3u8.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.DownloaderScenes;

public class NoFFMpegInstallationController {

    @FXML
    public void installFfmpeg(ActionEvent actionEvent) {
        // Also initializes the installation
        Main.mainWindowController.swapScenes(DownloaderScenes.INSTALLING);
    }

    @FXML
    public void quit(ActionEvent actionEvent) {
        Main.quit();
    }
}
