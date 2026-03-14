package com.lsn.client;

import com.lsn.client.auth.AuthClient;
import com.lsn.client.repository.HttpSocialNetworkRepository;
import com.lsn.client.service.SocialNetworkService;
import com.lsn.client.websocket.FeedListener;

import java.time.Clock;
import java.util.List;
import java.util.Scanner;

public class Application {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        CommandParser parser = new CommandParser();
        AuthClient authClient = new AuthClient();

        System.out.print("Server URL [http://localhost:8383]: ");
        String serverUrl = scanner.nextLine().trim();

        if (serverUrl.isBlank()) {
            serverUrl = "http://localhost:8383";
        }

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("(l)ogin or (r)egister? ");
        String choice = scanner.nextLine().trim().toLowerCase();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        String token;

        try {

            if ("r".equals(choice)) {
                authClient.register(serverUrl, username, password);
                System.out.println("Registered successfully.");
            }

            token = authClient.login(serverUrl, username, password);
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            return;
        }

        FeedListener listener = new FeedListener(serverUrl, token);
        listener.start();

        SocialNetworkService service = new SocialNetworkService(
                new HttpSocialNetworkRepository(serverUrl, token),
                Clock.systemUTC());

        System.out.println("Connected as " + username + ". Type 'exit' to quit.");

        while (scanner.hasNextLine()) {

            String line = scanner.nextLine().trim();

            if ("exit".equals(line)) {
                listener.disconnect();
                System.exit(0);
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
