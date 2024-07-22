package io.github.defectly;

import java.io.IOException;

public class Main {

    static Server server;

    public static void main(String[] args) {
//
//        try {
//            server = new Server("127.0.0.1", 35);
//        } catch (IOException exception) {
//            System.out.println(exception.getMessage());
//            throw new RuntimeException(exception);
//        }
//
//        server.start();

        var window = new MainWindow();
        window.setVisible(true);
    }
}