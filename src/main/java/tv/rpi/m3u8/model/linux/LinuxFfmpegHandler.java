package tv.rpi.m3u8.model.linux;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.model.common.AbstractFfmpegHandler;
import tv.rpi.m3u8.model.common.M3U8FileUtil;
import tv.rpi.m3u8.model.common.ProgressFeedback;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class LinuxFfmpegHandler extends AbstractFfmpegHandler {
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
                "/usr/bin/ffmpeg"
        });
    }

    @Override
    public void install(ProgressFeedback feedback) throws IOException, ArchiveException {
        Main.LOGGER.info("Beginning download of ffmpeg...");

        final String downloadSourcePath = "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz";
        final String downloadDestinationFolderPath = FileUtils.getTempDirectoryPath() + "m3u8/";
        final String downloadDestinationPath = downloadDestinationFolderPath + "ffmpeg.tar.xz";
        final File downloadedFile = M3U8FileUtil.downloadFile(downloadSourcePath, downloadDestinationPath,
                ((percentage, details) -> feedback.callback(percentage * 0.4, "Downloading ffmpeg...")));

        // ----

        Main.LOGGER.info("Download complete. Decompressing...");

        feedback.callback(0.4, "Decompressing...");
        final File decompressedTar = new File(downloadDestinationFolderPath + "ffmpeg.tar");
        M3U8FileUtil.decompressXz(downloadedFile, decompressedTar, ((percentage, details) ->
                feedback.callback(0.4 + 0.25 * percentage, "Decompressing...")));

        Main.LOGGER.info("Decompressing complete. Unzipping...");

        feedback.callback(0.6, "Unzipping...");
        final String unzipDestinationPath = downloadDestinationFolderPath + "unzipped";
        final File unzipDestination = new File(unzipDestinationPath);
        M3U8FileUtil.extractArchive(decompressedTar, unzipDestination, ArchiveStreamFactory.TAR, ((percentage, details) ->
                feedback.callback(0.65 + 0.25 * percentage, "Unzipping...")));

        Main.LOGGER.info("Unzipped. Installing...");

        feedback.callback(0.9, "Collecting files...");
        final Collection<File> unzippedFiles = FileUtils.listFilesAndDirs(unzipDestination,
                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        feedback.callback(0.95, "Installing...");
        final String installLocationPath = System.getenv("HOME") + "/ffmpeg/";
        final File installLocation = new File(installLocationPath + "bin");
        FileUtils.createParentDirectories(installLocation);
        float searchedFileCount = 0; // Used for progress bar
        for(final File f : unzippedFiles) {
            feedback.callback(0.95 + (searchedFileCount / unzippedFiles.size()) * 0.1, "Installing...");
            // We want to install these binary files.
            if(f.getName().equals("ffmpeg") || f.getName().equals("ffprobe")) {
                FileUtils.copyFile(f, new File(installLocationPath + "/bin/" + f.getName()));
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
