package tv.rpi.m3u8.model.windows;

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
}
