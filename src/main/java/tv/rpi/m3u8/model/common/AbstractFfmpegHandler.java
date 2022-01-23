package tv.rpi.m3u8.model.common;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.linux.LinuxFfmpegHandler;
import tv.rpi.m3u8.model.windows.WindowsFfmpegHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractFfmpegHandler {
    public abstract boolean isFfmpegInstalled() throws IOException, InterruptedException;
    public abstract String getFfmpegPath() throws IOException, InterruptedException;
    public abstract void install(ProgressFeedback feedback) throws IOException, ArchiveException;

    protected String getFfmpegPathBase(String cmd, String[] possiblePaths) throws IOException, InterruptedException {
        final Process pathProcess = Runtime.getRuntime().exec(cmd);
        Main.processList.add(pathProcess);
        final AtomicReference<String> output = new AtomicReference<>();

        Main.threadPool.submit(() -> {
            // Where only returns one line, so we only need the first line.
            final BufferedReader input = new BufferedReader(new InputStreamReader(pathProcess.getInputStream()));
            try {
                output.set(input.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        pathProcess.waitFor();
        Main.processList.remove(pathProcess);

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

    public int getSegmentCount(String input, ProgressFeedback feedback) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(input).openConnection();
        final int contentLength = connection.getContentLength();
        final InputStream stream = connection.getInputStream();

        final StringBuilder lineBuilder = new StringBuilder();
        int segmentCount = 0;
        int totalBytesRead = 0;
        int b;
        while((b = stream.read()) > 0) {
            totalBytesRead++;
            lineBuilder.append((char) b);
            if(b == '\n') {
                // Don't need to provide status updates so often... Lines in m3u8 files are short.
                if(feedback != null) {
                    feedback.callback((double) totalBytesRead / (double) contentLength, null);
                }
                if(lineBuilder.toString().startsWith("#EXTINF")) {
                    segmentCount++;
                }
                lineBuilder.delete(0, lineBuilder.length());
            }
        }
        return segmentCount;
    }

    protected String getFileNameFromSourceUrl(String input) {
        final String fileNameWithExtension = new File(input).getName();
        final String[] fileNameSplitAtDot = fileNameWithExtension.split("\\.");
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < fileNameSplitAtDot.length - 1; i++) {
            builder.append(fileNameSplitAtDot[i]);
        }
        if(builder.length() == 0) {
            return fileNameWithExtension;
        } else {
            return builder.toString();
        }
    }

    public abstract void download(String m3u8, String outputDir, int compressionValue, ProgressFeedback feedback) throws IOException, InterruptedException;
}
