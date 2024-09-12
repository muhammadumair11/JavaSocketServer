package org.javasocketserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class Server {
    private ServerSocket socket;
    private Socket client;
    private PrintWriter messageSender;
    private BufferedReader messageReader;
    private InputStream dataComingIn;
    private boolean running;

    private Thread receiverThread;
    private Thread senderThread;


    public Server(int port) throws IOException {
        socket = new ServerSocket(port);
        System.out.println("Creater Running on PORT:    " + port);

        client = socket.accept();
        dataComingIn = client.getInputStream();

        messageSender = new PrintWriter(client.getOutputStream(), true);
        messageReader = new BufferedReader(new InputStreamReader(dataComingIn));

        receiverThread = new Thread(this::messageReceiver);

        senderThread = new Thread(() -> {
            try {
                messageSendingService();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        receiverThread.start();
        senderThread.start();

        try {
            receiverThread.join();
            senderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Properly handle interrupt
            e.printStackTrace();
        } finally {
            closeConnection(); // Ensure resources are closed
        }

    }

    public void messageSendingService() throws IOException {
        Scanner readConsole = new Scanner(System.in);
        System.out.println("Type exit to stop service");

        while(running) {
            if(Objects.equals(readConsole.nextLine(), "exit")) {
                closeConnection();
                running = false;
                readConsole.close();
                return;
            }
            sendMessage(readConsole.nextLine());
        }
    }


    private void sendMessage(String message) {
        messageSender.println(message);
        messageSender.flush();
    }

    private void messageReceiver() {
        running = true;

        try {

            String receivedMessage;
            while (running && (receivedMessage = messageReader.readLine()) != null) {
                System.out.println("Received:  " + receivedMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection(){
        try {
            if (messageReader != null) messageReader.close();
            if (messageSender != null) messageSender.close();
            if (client != null) client.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
