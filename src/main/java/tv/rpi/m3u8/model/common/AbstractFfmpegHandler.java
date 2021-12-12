package tv.rpi.m3u8.model.common;

import org.apache.commons.compress.archivers.ArchiveException;
import tv.rpi.m3u8.model.linux.LinuxFfmpegHandler;
import tv.rpi.m3u8.model.windows.WindowsFfmpegHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractFfmpegHandler {
    public abstract boolean isFfmpegInstalled() throws IOException, InterruptedException;
    public abstract String getFfmpegPath() throws IOException, InterruptedException;
    public abstract void install(ProgressFeedback feedback) throws IOException, ArchiveException;

    protected String getFfmpegPathBase(String cmd, String[] possiblePaths) throws IOException, InterruptedException {
        final Process pathProcess = Runtime.getRuntime().exec(cmd);
        final AtomicReference<String> output = new AtomicReference<>();

        new Thread(() -> {
            // Where only returns one line, so we only need the first line.
            final BufferedReader input = new BufferedReader(new InputStreamReader(pathProcess.getInputStream()));
            try {
                output.set(input.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        pathProcess.waitFor();

        // Search for ffmpeg installation outside the path.
        if(output.get() == null) {
            for (final String possiblePath : possiblePaths) {
                final File tmp = new File(possiblePath);
                if (tmp.exists()) {
                    return possiblePath;
                }
            }
        }
        return output.get();
    }

    public static AbstractFfmpegHandler getForOperatingSystem(OperatingSystem os) {
        switch(os) {
            case WINDOWS:
                return new WindowsFfmpegHandler();
            case MAC:
            case LINUX:
            case OTHER:
            default:
                return new LinuxFfmpegHandler();
        }
    }
}
