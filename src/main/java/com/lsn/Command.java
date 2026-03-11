package com.lsn;

public sealed interface Command {

    record ReadCommand(String user) implements Command {}

    record PostCommand(String user, String message) implements Command {}

    record FollowCommand(String user, String target) implements Command {}

    record WallCommand(String user) implements Command {}
}
