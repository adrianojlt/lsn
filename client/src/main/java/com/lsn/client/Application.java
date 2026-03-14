package com.lsn.client;

import com.lsn.client.repository.InMemoryRepository;
import com.lsn.client.service.SocialNetworkService;

import java.time.Clock;
import java.util.List;
import java.util.Scanner;

public class Application {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        CommandParser parser = new CommandParser();
        SocialNetworkService service = new SocialNetworkService(new InMemoryRepository(), Clock.systemUTC());

        while (scanner.hasNextLine()) {

            String line = scanner.nextLine().trim();

            if ("exit".equals(line)) {
                break;
            }

            parser.parse(line).ifPresentOrElse(
                    cmd -> handle(cmd, service),
                    ()  -> System.out.println("Unknown command: " + line)
            );
        }
    }

    private static void handle(Command cmd, SocialNetworkService service) {
        switch (cmd) {
            case Command.PostCommand   c -> service.post(c.user(), c.message());
            case Command.ReadCommand   c -> print(service.read(c.user()));
            case Command.FollowCommand c -> service.follow(c.user(), c.target());
            case Command.WallCommand   c -> print(service.wall(c.user()));
        }
    }

    private static void print(List<String> lines) {
        lines.forEach(System.out::println);
    }
}
