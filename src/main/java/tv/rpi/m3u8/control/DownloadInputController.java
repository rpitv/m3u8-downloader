package tv.rpi.m3u8.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.DownloaderScenes;

import java.io.File;

public class DownloadInputController {

    @FXML
    public Label outputDirectory;
    @FXML
    public TextField m3u8Link;
    @FXML
    public Button downloadButton;
    @FXML
    public Label inputIssues;
    @FXML
    public Slider compressionSlider;

    public void initialize() {
        final String lastM3u8 = Main.getSettings().lastUsedM3u8;
        final String lastOutputDir = Main.getSettings().lastUsedOutputDir;
        final double lastCompressionValue = Main.getSettings().compressionValue;

        if(lastM3u8 != null) {
            this.m3u8Link.setText(lastM3u8);
        }
        if(lastOutputDir != null) {
            this.outputDirectory.setText(lastOutputDir);
        }
        this.compressionSlider.setValue(lastCompressionValue);

        // Perform checks to determine the state of the input button, but don't warn the user about input issues yet.
        this.performFormChecks();
        this.inputIssues.setText("");
    }

    @FXML
    public void selectDirectory(ActionEvent actionEvent) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        if (this.outputDirectory.getText() != null && this.outputDirectory.getText().length() > 0) {
            final File currentlySelectedDirectory = new File(this.outputDirectory.getText());
            directoryChooser.setInitialDirectory(currentlySelectedDirectory);
        } else {
            directoryChooser.setInitialDirectory(new File("."));
        }
        final File selectedDirectory = directoryChooser.showDialog(Main.mainStage);
        if(selectedDirectory != null) {
            Main.LOGGER.debug("Selected output directory: {}", selectedDirectory.getAbsolutePath());
            this.outputDirectory.setText(selectedDirectory.getAbsolutePath());
            Main.getSettings().lastUsedOutputDir = selectedDirectory.getAbsolutePath();
        }
        this.performFormChecks();
    }

    @FXML
    public void startDownload(ActionEvent actionEvent) {
        if(!this.performFormChecks()) {
            return;
        }

        Main.mainWindowController.swapScenes(DownloaderScenes.DOWNLOADING);
    }

    @FXML
    public void linkInput(KeyEvent actionEvent) {
        Main.getSettings().lastUsedM3u8 = this.m3u8Link.getText();
        this.performFormChecks();
    }

    @FXML
    public void compressionSliderUpdate(MouseEvent mouseEvent) {
        Main.getSettings().compressionValue = this.compressionSlider.getValue();
        this.performFormChecks();
    }

    private boolean performFormChecks() {
        if(!this.m3u8Link.getText().endsWith(".m3u8")) {
            this.inputIssues.setText("Please input a link to a .m3u8 file.");
            this.disableDownloadButton();
            return false;
        } else if (this.outputDirectory.getText() == null || !new File(this.outputDirectory.getText()).exists()) {
            this.inputIssues.setText("Please select a valid output directory.");
            this.disableDownloadButton();
            return false;
        } else if (this.compressionSlider.getValue() > 51 || this.compressionSlider.getValue() < 0) {
            this.inputIssues.setText("Please select a valid compression value.");
            this.disableDownloadButton();
            return false;
        }
        this.inputIssues.setText("");
        this.enableDownloadButton();
        return true;
    }

    private void enableDownloadButton() {
        this.downloadButton.getStyleClass().removeIf((styleClass) -> {
            return styleClass.equals("disabled");
        });
    }

    private void disableDownloadButton() {
        if(!this.downloadButton.getStyleClass().contains("disabled")) {
            this.downloadButton.getStyleClass().add("disabled");
        }
    }
}
