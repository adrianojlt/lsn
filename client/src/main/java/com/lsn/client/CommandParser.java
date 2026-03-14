package com.lsn.client;

import java.util.Optional;

public class CommandParser {

    private static final String WALL_SUFFIX = " wall";
    private static final String POST_SEPARATOR = " -> ";
    private static final String FOLLOWS_KEYWORD = " follows ";

    public Optional<Command> parse(String input) {

        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        if (input.contains(POST_SEPARATOR)) {
            String[] parts = input.split(POST_SEPARATOR, 2);
            return Optional.of(new Command.PostCommand(parts[0].trim(), parts[1].trim()));
        }

        if (input.contains(FOLLOWS_KEYWORD)) {
            String[] parts = input.split(FOLLOWS_KEYWORD, 2);
            return Optional.of(new Command.FollowCommand(parts[0].trim(), parts[1].trim()));
        }

        if (input.endsWith(WALL_SUFFIX)) {
            String user = input.substring(0, input.length() - WALL_SUFFIX.length()).trim();
            return Optional.of(new Command.WallCommand(user));
        }

        if (!input.contains(" ")) {
            return Optional.of(new Command.ReadCommand(input.trim()));
        }

        return Optional.empty();
    }
}
