package tv.rpi.m3u8.model.windows;

import javafx.application.Platform;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.DownloaderScenes;
import tv.rpi.m3u8.model.common.AbstractFfmpegHandler;
import tv.rpi.m3u8.model.common.M3U8FileUtil;
import tv.rpi.m3u8.model.common.ProgressFeedback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.org.apache.xalan.internal.lib.ExsltStrings.split;

public class WindowsFfmpegHandler extends AbstractFfmpegHandler {
    @Override
    public boolean isFfmpegInstalled() throws IOException, InterruptedException {
        return this.getFfmpegPath() != null;
    }

    @Override
    public String getFfmpegPath() throws IOException, InterruptedException {
        return this.getFfmpegPathBase("cmd /c where ffmpeg", new String[]{
                System.getenv("HOME") + "\\ffmpeg\\bin\\ffmpeg.exe",
                "\\ffmpeg\\bin\\ffmpeg.exe",
                "\\Program Files\\ffmpeg\\bin\\ffmpeg.exe",
                "\\Program Files (x86)\\ffmpeg\\bin\\ffmpeg.exe"
        });
    }

    @Override
    public void install(ProgressFeedback feedback) throws IOException, ArchiveException {
        Main.LOGGER.info("Beginning download of ffmpeg...");

        final String downloadSourcePath = "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip";
        final String downloadDestinationFolderPath = FileUtils.getTempDirectoryPath() + "m3u8\\";
        final String downloadDestinationPath = downloadDestinationFolderPath + "ffmpeg.zip";
        final File downloadedFile = M3U8FileUtil.downloadFile(downloadSourcePath, downloadDestinationPath,
                ((percentage, details) -> feedback.callback(percentage * 0.5, "Downloading ffmpeg...")));

        // ----

        feedback.callback(0.5, "Unzipping...");
        Main.LOGGER.info("Download complete. Unzipping...");
        final String unzipDestinationPath = downloadDestinationFolderPath + "unzipped";
        final File unzipDestination = new File(unzipDestinationPath);
        M3U8FileUtil.extractArchive(downloadedFile, unzipDestination, ArchiveStreamFactory.ZIP, ((percentage, details) ->
                feedback.callback(0.5 + percentage * 0.25, "Unzipping...")));

        // ----

        Main.LOGGER.info("Unzipped. Installing...");
        feedback.callback(0.75, "Collecting files...");
        final Collection<File> unzippedFiles = FileUtils.listFilesAndDirs(unzipDestination,
                FalseFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        // ----

        final String installLocationPath = System.getenv("HOME") + "\\ffmpeg\\";
        float searchedDirectoryCount = 0; // Used for progress bar
        for(final File f : unzippedFiles) {
            feedback.callback(0.8 + (searchedDirectoryCount / unzippedFiles.size()) * 0.1, "Installing...");
            if(f.getName().equals("bin")) { // We want to install just the binary files.
                final File installLocation = new File(installLocationPath + "bin");
                FileUtils.createParentDirectories(installLocation);

                final Collection<File> binaryFiles = FileUtils.listFiles(f, TrueFileFilter.INSTANCE, FalseFileFilter.FALSE);
                float transferredBinaryCount = 0; // Used for progress bar
                for(final File binary : binaryFiles) {
                    feedback.callback(0.9 + (transferredBinaryCount / binaryFiles.size()) * 0.1, "Installing...");
                    FileUtils.copyFile(binary, new File(installLocationPath + "\\bin\\" + binary.getName()));
                }
                break;
            }
            searchedDirectoryCount++;
        }
        feedback.callback(1.0, "Installation complete.");

        Main.LOGGER.info("Installation complete at {}.", installLocationPath);

        // TODO add to path
    }

    @Override
    public void download(String input, String outputDir, int compressionValue, ProgressFeedback feedback) throws IOException, InterruptedException {
        final String ffmpegPath = this.getFfmpegPath();
        final String command = "cmd /c " + ffmpegPath + " -i \"" + input + "\" -n -bsf:a aac_adtstoasc -c copy -crf " + compressionValue +
                " \"" + outputDir + "\\" + this.getFileNameFromSourceUrl(input) + ".mp4\"";
        Main.LOGGER.debug("Full download command: {}", command);

        final int segmentCount = this.getSegmentCount(input, ((percentage, details) -> {
            if(feedback == null) {
                return;
            }
            feedback.callback(percentage * 0.1, "Calculating file sizes...");
        }));
        final Process downloadProcess = Runtime.getRuntime().exec(command);
        Main.processList.add(downloadProcess);

        Main.threadPool.submit(() -> {
            final InputStream is = downloadProcess.getErrorStream();
            final StringBuilder lineBuilder = new StringBuilder();
            // Note: possible issues with m3u8 files which have numbers in their titles.
            final Pattern segmentNumberPattern = Pattern.compile("(\\d+)\\.ts\\' for reading$");
            int b;
            while(true) {
                try {
                    if ((b = is.read()) < 0) break;
                    if (b == '\n') {
                        final String str = lineBuilder.toString();
                        lineBuilder.delete(0, lineBuilder.length());
                        Main.LOGGER.debug(str);
                        // Error... FIXME It's possible the download could still continue if only some segments are missing?
                        //           Not destroying the process for this reason.
                        //           NOTE: Looked more into this and failed segments seem to contain "Failed". Look for "Fail" and document that post-download?
                        if(str.contains("Server returned") || str.contains("already exists. Exiting")) {
                            Platform.runLater(() -> {
                                Main.errorMessageToDisplay = str;
                                Main.mainWindowController.swapScenes(DownloaderScenes.ERROR);
                            });
                        } else if(feedback != null) {
                            final Matcher segmentNumberMatcher = segmentNumberPattern.matcher(str);
                            if(segmentNumberMatcher.find()) {
                                final String segmentNumberStr = segmentNumberMatcher.group(1);
                                final int segmentNumber = Integer.parseInt(segmentNumberStr) + 1; // + 1 since segments are typically zero-indexed. Actual solution is to read #EXT-X-MEDIA-SEQUENCE
                                Platform.runLater(() -> {
                                    feedback.callback(0.1 + 0.9 * ((double) segmentNumber / (double) segmentCount),
                                            "Downloading (" + segmentNumber + "/" + segmentCount + ")");
                                });
                            }
                        }
                    } else {
                        lineBuilder.append((char) b);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        downloadProcess.waitFor();
        Main.processList.remove(downloadProcess);
    }
}
