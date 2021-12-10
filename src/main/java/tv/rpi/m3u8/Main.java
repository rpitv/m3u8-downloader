package tv.rpi.m3u8;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tv.rpi.m3u8.common.IFfmpegHandler;

import java.io.IOException;
import java.util.Locale;

public class Main {

    public static Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        final OperatingSystem os = Main.getOperatingSystem();
        IFfmpegHandler ffmpeg = IFfmpegHandler.getForOperatingSystem(os);
        try {
            if(!ffmpeg.isFfmpegInstalled()) {
                logger.fatal("ffmpeg is not installed!");
                System.exit(1);
                return;
            }

            String path = ffmpeg.getFfmpegPath();
            logger.debug("ffmpeg path: {}", path);
        } catch (IOException | InterruptedException e) {
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
