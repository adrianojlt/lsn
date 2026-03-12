package com.lsn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class SocialNetworkIntegrationTest {

    private static final Instant T0 = Instant.parse("2026-03-11T12:05:00Z");

    private MutableClock clock;
    private SocialNetworkService service;
    private CommandParser parser;

    @BeforeEach
    void setUp() {
        clock   = new MutableClock(Clock.fixed(T0, ZoneOffset.UTC));
        service = new SocialNetworkService(new InMemoryRepository(), clock);
        parser  = new CommandParser();
    }

    @Test
    void fullScenario() {

        clock.set(T0.minus(5, MINUTES));
        execute("Alice -> I love the weather today");

        clock.set(T0.minus(2, MINUTES));
        execute("Bob -> Damn! We lost!");

        clock.set(T0.minus(1, MINUTES));
        execute("Bob -> Good game though.");

        clock.set(T0);
        assertThat(execute("Alice"))
                .containsExactly("I love the weather today (5 minutes ago)");

        assertThat(execute("Bob"))
                .containsExactly(
                        "Good game though. (1 minute ago)",
                        "Damn! We lost! (2 minutes ago)"
                );

        clock.set(T0.minus(2, SECONDS));
        execute("Charlie -> I'm in New York today! Anyone want to hang out?");

        clock.set(T0);
        execute("Charlie follows Alice");

        assertThat(execute("Charlie wall"))
                .containsExactly(
                        "Charlie - I'm in New York today! Anyone want to hang out? (2 seconds ago)",
                        "Alice - I love the weather today (5 minutes ago)"
                );

        clock.set(T0.plus(13, SECONDS));
        execute("Charlie follows Bob");

        assertThat(execute("Charlie wall"))
                .containsExactly(
                        "Charlie - I'm in New York today! Anyone want to hang out? (15 seconds ago)",
                        "Bob - Good game though. (1 minute ago)",
                        "Bob - Damn! We lost! (2 minutes ago)",
                        "Alice - I love the weather today (5 minutes ago)"
                );
    }

    private List<String> execute(String input) {
        Command cmd = parser.parse(input).orElseThrow();
        return switch (cmd) {
            case Command.PostCommand   c -> { service.post(c.user(), c.message());     yield List.of(); }
            case Command.ReadCommand   c -> service.read(c.user());
            case Command.FollowCommand c -> { service.follow(c.user(), c.target());   yield List.of(); }
            case Command.WallCommand   c -> service.wall(c.user());
        };
    }

    static class MutableClock extends Clock {

        private Clock delegate;

        MutableClock(Clock initial) {
            this.delegate = initial;
        }

        void set(Instant instant) {
            this.delegate = Clock.fixed(instant, ZoneOffset.UTC);
        }

        @Override
        public ZoneId getZone() {
            return delegate.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return delegate.withZone(zone);
        }

        @Override
        public Instant instant() {
            return delegate.instant();
        }
    }
}
