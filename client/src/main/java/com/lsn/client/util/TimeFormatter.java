package com.lsn.client.util;

import java.time.Duration;

public class TimeFormatter {

    public static String format(Duration duration) {

        long seconds = duration.toSeconds();
        long minutes = duration.toMinutes();
        long hours   = duration.toHours();

        if (hours >= 1) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        }

        if (minutes >= 1) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        }

        return seconds + " seconds ago";
    }
}
