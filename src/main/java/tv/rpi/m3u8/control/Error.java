package tv.rpi.m3u8.control;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tv.rpi.m3u8.Main;

public class Error {

    @FXML
    public Label errorMsg;

    public void initialize() {
        if(Main.errorMessageToDisplay != null) {
            errorMsg.setText(Main.errorMessageToDisplay);
        }
    }
}
