package tv.rpi.m3u8.linux;

import tv.rpi.m3u8.common.IFfmpegHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

public class LinuxFfmpegHandler implements IFfmpegHandler {
    @Override
    public boolean isFfmpegInstalled() throws IOException, InterruptedException {
        return this.getFfmpegPath() != null;
    }

    @Override
    public String getFfmpegPath() throws IOException, InterruptedException {
        final Process whereProcess = Runtime.getRuntime().exec("which ffmpeg");
        AtomicReference<String> output = new AtomicReference<>();

        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(whereProcess.getInputStream()));
            try {
                output.set(input.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        whereProcess.waitFor();
        return output.get();
    }
}
