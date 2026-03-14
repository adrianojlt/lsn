package com.lsn.client;

public sealed interface Command {
    record WallCommand(String user) implements Command {}
    record ReadCommand(String user) implements Command {}
    record PostCommand(String user, String message) implements Command {}
    record FollowCommand(String user, String target) implements Command {}
}
