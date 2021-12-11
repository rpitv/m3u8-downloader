package tv.rpi.m3u8.windows;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import tv.rpi.m3u8.Main;
import tv.rpi.m3u8.common.AbstractFfmpegHandler;
import tv.rpi.m3u8.common.CompressedFileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    public void install() throws IOException, ArchiveException {
        System.out.println("Beginning download of ffmpeg...");
        Main.LOGGER.info("Beginning download of ffmpeg...");

        final String downloadZipPath = "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip";
        final String destinationFolder = FileUtils.getTempDirectoryPath() + "m3u8\\";
        final File downloadLocation = new File(destinationFolder + "ffmpeg.zip");
        FileUtils.createParentDirectories(downloadLocation);
        FileUtils.copyURLToFile(new URL(downloadZipPath), downloadLocation, 30000, 30000); // Connection timeout after 10 seconds

        System.out.println("Download complete. Unzipping...");
        Main.LOGGER.info("Download complete. Unzipping...");

        final String unzipDestinationPath = destinationFolder + "unzipped";
        final File unzipDestination = new File(unzipDestinationPath);
        CompressedFileUtil.extractArchive(downloadLocation, unzipDestination, ArchiveStreamFactory.ZIP);

        System.out.println("Unzipped. Installing...");
        Main.LOGGER.info("Unzipped. Installing...");

        final Collection<File> unzippedFiles = FileUtils.listFilesAndDirs(unzipDestination,
                FalseFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        final String installLocationPath = System.getenv("HOME") + "\\ffmpeg\\";
        for(final File f : unzippedFiles) {
            if(f.getName().equals("bin")) { // We want to install the binary files.
                final File installLocation = new File(installLocationPath + "bin");
                FileUtils.createParentDirectories(installLocation);
                final Collection<File> binaryFiles = FileUtils.listFiles(f, TrueFileFilter.INSTANCE, FalseFileFilter.FALSE);
                for(final File binary : binaryFiles) {
                    FileUtils.copyFile(binary, new File(installLocationPath + "\\bin\\" + binary.getName()));
                }
                break;
            }
        }

        System.out.printf("Installation complete at %s. Cleaning up...\n", installLocationPath);
        Main.LOGGER.info("Installation complete at {}. Cleaning up...", installLocationPath);

        Main.LOGGER.debug("Attempting to delete temporary files at {}.", destinationFolder);
        boolean wasDeleted = new File(destinationFolder).delete();
        if(!wasDeleted) {
            Main.LOGGER.debug("Was not able to delete the temporary files from {}.", destinationFolder);
        }

        // TODO add to path
    }
}
