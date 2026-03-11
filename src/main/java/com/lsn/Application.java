package com.lsn;

import java.util.Scanner;

public class Application {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {

            String line = scanner.nextLine();

            if ("exit".equals(line)) {
                break;
            }

            System.out.println(line);
        }
    }
}
