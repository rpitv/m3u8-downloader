package tv.rpi.m3u8.common;

import tv.rpi.m3u8.OperatingSystem;
import tv.rpi.m3u8.linux.LinuxFfmpegHandler;
import tv.rpi.m3u8.windows.WindowsFfmpegHandler;

import java.io.IOException;

public interface IFfmpegHandler {
    boolean isFfmpegInstalled() throws IOException, InterruptedException;
    String getFfmpegPath() throws IOException, InterruptedException;

    static IFfmpegHandler getForOperatingSystem(OperatingSystem os) {
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
