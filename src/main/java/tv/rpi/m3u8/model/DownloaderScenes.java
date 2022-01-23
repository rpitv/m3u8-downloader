package tv.rpi.m3u8.model;

public enum DownloaderScenes {
    MAIN_WINDOW("mainWindow"),
    INSTALLING("installing"),
    ERROR("error"),
    NO_FFMPEG_INSTALLATION("noFFMpegInstallation"),
    LOADING_SPINNER("loading-spinner"),
    DOWNLOAD_INPUT("download-input"),
    DOWNLOADING("downloading");


    final String fileName;

    public String getPath() {
        return "/scenes/" + this.fileName + ".fxml";
    }

    DownloaderScenes(final String fileName) {
        this.fileName = fileName;
    }
}
