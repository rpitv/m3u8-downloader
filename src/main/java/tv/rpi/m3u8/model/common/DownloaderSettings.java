package tv.rpi.m3u8.model.common;

import tv.rpi.m3u8.Main;

import java.io.*;

public class DownloaderSettings implements Serializable {
    public String lastUsedM3u8;
    public String lastUsedOutputDir;
    public double compressionValue = 25;

    private final String outputFile;

    public DownloaderSettings(String settingsFile) {
        outputFile = settingsFile;
    }

    public void save() throws IOException {
        final File file = new File(this.outputFile);
        if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            Main.LOGGER.warn("Unable to make parent directories for settings file.");
        }
        final FileOutputStream fileOutputStream = new FileOutputStream(file);
        final ObjectOutputStream serializer = new ObjectOutputStream(fileOutputStream);
        serializer.writeObject(this);
        serializer.close();
        fileOutputStream.close();
    }
}
