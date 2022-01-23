package tv.rpi.m3u8.model.macos;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.common.AbstractFfmpegHandler;
import tv.rpi.m3u8.model.common.M3U8FileUtil;
import tv.rpi.m3u8.model.common.ProgressFeedback;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class MacOsFfmpegHandler extends AbstractFfmpegHandler {
    @Override
    public boolean isFfmpegInstalled() throws IOException, InterruptedException {
        return this.getFfmpegPath() != null;
    }

    @Override
    public String getFfmpegPath() throws IOException, InterruptedException {
        return this.getFfmpegPathBase("which ffmpeg", new String[]{
                "~/bin/ffmpeg",
                "/ffmpeg/bin/ffmpeg",
                "/usr/local/bin/ffmpeg",
                "/usr/bin/ffmpeg",
                "/Applications/ffmpeg"
        });
    }

    @Override
    public void install(ProgressFeedback feedback) throws IOException, ArchiveException {
        Main.LOGGER.info("Beginning download of ffmpeg...");

        final String downloadSourcePath = "https://evermeet.cx/ffmpeg/getrelease/zip";
        final String downloadDestinationFolderPath = FileUtils.getTempDirectoryPath() + "m3u8/";
        final String downloadDestinationPath = downloadDestinationFolderPath + "ffmpeg.zip";
        final File downloadedFile = M3U8FileUtil.downloadFile(downloadSourcePath, downloadDestinationPath,
                ((percentage, details) -> feedback.callback(percentage * 0.5, "Downloading ffmpeg...")));

        // ----

        feedback.callback(0.5, "Unzipping...");
        Main.LOGGER.info("Download complete. Unzipping...");
        final String unzipDestinationPath = downloadDestinationFolderPath + "unzipped";
        final File unzipDestination = new File(unzipDestinationPath);
        M3U8FileUtil.extractArchive(downloadedFile, unzipDestination, ArchiveStreamFactory.ZIP, ((percentage, details) ->
                feedback.callback(0.5 + percentage * 0.30, "Unzipping...")));

        // ----

        Main.LOGGER.info("Unzipped. Installing...");
        feedback.callback(0.80, "Collecting files...");
        final Collection<File> unzippedFiles = FileUtils.listFilesAndDirs(unzipDestination,
                FalseFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        // ----

        feedback.callback(0.9, "Installing...");
        final String installLocationPath = System.getenv("HOME") + "/ffmpeg/";
        final File installLocation = new File(installLocationPath + "bin");
        FileUtils.createParentDirectories(installLocation);
        float searchedFileCount = 0; // Used for progress bar
        for(final File f : unzippedFiles) {
            feedback.callback(0.9 + (searchedFileCount / unzippedFiles.size()) * 0.1, "Installing...");
            // We want to install these binary files.
            if(f.getName().equals("ffmpeg")) {
                FileUtils.copyFile(f, new File(installLocationPath + "/bin/" + f.getName()));
                break;
            }
            searchedFileCount++;
        }
        feedback.callback(1.0, "Installation complete.");

        Main.LOGGER.info("Installation complete at {}.", installLocationPath);

        // TODO add to path
    }

    @Override
    public void download(String m3u8, String outputDir, int compressionValue, ProgressFeedback feedback) throws IOException, InterruptedException {

    }
}
