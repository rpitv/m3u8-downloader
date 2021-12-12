package tv.rpi.m3u8.model.common;

public interface ProgressFeedback  {
    void callback(double percentage, String details);
}

