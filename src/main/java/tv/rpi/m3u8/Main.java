package tv.rpi.m3u8;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tv.rpi.m3u8.common.AbstractFfmpegHandler;
import tv.rpi.m3u8.linux.LinuxFfmpegHandler;
import tv.rpi.m3u8.macos.MacOsFfmpegHandler;

import java.io.IOException;
import java.util.Locale;

public class Main {

    public static Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
//        MacOsFfmpegHandler macTest = new MacOsFfmpegHandler();
//        try {
//            macTest.install();
//        } catch (IOException | ArchiveException e) {
//            e.printStackTrace();
//        }
//
//        if(true) {
//            return;
//        }

        final OperatingSystem os = Main.getOperatingSystem();
        AbstractFfmpegHandler ffmpeg = AbstractFfmpegHandler.getForOperatingSystem(os);
        try {
            if(!ffmpeg.isFfmpegInstalled()) {
                LOGGER.fatal("ffmpeg is not installed!");
                ffmpeg.install();
                System.exit(1);
                return;
            }

            String path = ffmpeg.getFfmpegPath();
            LOGGER.debug("ffmpeg path: {}", path);
        } catch (IOException | InterruptedException | ArchiveException e) {
            e.printStackTrace();
        }
    }

    private static OperatingSystem getOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.contains("windows")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("mac")) {
            return OperatingSystem.MAC;
        } else if (osName.contains("linux")) {
            return OperatingSystem.LINUX;
        } else {
            return OperatingSystem.OTHER;
        }
    }
}
