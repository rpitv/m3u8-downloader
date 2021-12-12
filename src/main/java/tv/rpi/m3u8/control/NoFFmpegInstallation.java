package tv.rpi.m3u8.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import tv.rpi.m3u8.Main;

public class NoFFmpegInstallation {

    @FXML
    public void installFfmpeg(ActionEvent actionEvent) {
        // Also initializes the installation
        Main.mainWindow.swapScenes("/scenes/installing.fxml");
    }

    @FXML
    public void quit(ActionEvent actionEvent) {
        System.exit(0);
    }
}
