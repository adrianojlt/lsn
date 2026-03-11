package com.lsn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    void parsesReadCommand() {
        assertThat(parser.parse("Alice"))
            .contains(new Command.ReadCommand("Alice"));
    }

    @Test
    void parsesPostCommand() {
        assertThat(parser.parse("Alice -> Hello world"))
            .contains(new Command.PostCommand("Alice", "Hello world"));
    }

    @Test
    void parsesFollowCommand() {
        assertThat(parser.parse("Charlie follows Alice"))
            .contains(new Command.FollowCommand("Charlie", "Alice"));
    }

    @Test
    void parsesWallCommand() {
        assertThat(parser.parse("Charlie wall"))
            .contains(new Command.WallCommand("Charlie"));
    }

    @Test
    void returnsEmptyForBlankInput() {
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse("   ")).isEmpty();
    }

    @Test
    void returnsEmptyForNullInput() {
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    void returnsEmptyForUnrecognisedInput() {
        assertThat(parser.parse("Alice does something unknown")).isEmpty();
    }
}
