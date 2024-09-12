package org.javasocketserver;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Server server = new Server(8082);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}