package com.lsn;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class TimeFormatterTest {

    @Test
    void oneSecond() {
        assertThat(TimeFormatter.format(Duration.ofSeconds(1))).isEqualTo("1 seconds ago");
    }

    @Test
    void fiftyNineSeconds() {
        assertThat(TimeFormatter.format(Duration.ofSeconds(59))).isEqualTo("59 seconds ago");
    }

    @Test
    void oneMinute() {
        assertThat(TimeFormatter.format(Duration.ofMinutes(1))).isEqualTo("1 minute ago");
    }

    @Test
    void twoMinutes() {
        assertThat(TimeFormatter.format(Duration.ofMinutes(2))).isEqualTo("2 minutes ago");
    }

    @Test
    void fiftyNineMinutes() {
        assertThat(TimeFormatter.format(Duration.ofMinutes(59))).isEqualTo("59 minutes ago");
    }

    @Test
    void oneHour() {
        assertThat(TimeFormatter.format(Duration.ofHours(1))).isEqualTo("1 hour ago");
    }

    @Test
    void twoHours() {
        assertThat(TimeFormatter.format(Duration.ofHours(2))).isEqualTo("2 hours ago");
    }
}
