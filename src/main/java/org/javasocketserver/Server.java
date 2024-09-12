package org.javasocketserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;
import java.util.Stack;

public class Server {
    private ServerSocket socket; // Server socket to accept client connections
    private Socket client; // Represents the client connection
    private PrintWriter messageSender; // Used to send messages to the client
    private BufferedReader messageReader; // Used to read messages from the client
    private InputStream dataComingIn; // InputStream for reading incoming data
    private boolean running; // Controls the server's running state

    private Thread receiverThread; // Thread to handle incoming messages from the client
    private Thread senderThread; // Thread to handle outgoing messages to the client

    Stack<String> messageStack = new Stack<>(); // Stack to store messages temporarily

    /**
     * Initializes the server on a specific port, sets up communication streams,
     * and starts the sender and receiver threads for handling messages.
     */
    public Server(int port) throws IOException {
        socket = new ServerSocket(port);
        System.out.println("Server Running on PORT: " + port);

        client = socket.accept();
        dataComingIn = client.getInputStream();

        messageSender = new PrintWriter(client.getOutputStream(), true);
        messageReader = new BufferedReader(new InputStreamReader(dataComingIn));

        receiverThread = new Thread(this::messageReceiver); // Thread to receive messages

        // Thread to handle outgoing messages, wrapped to handle exceptions
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
            receiverThread.join(); // Ensure both threads complete before proceeding
            senderThread.join();

            senderThread.setPriority(Thread.MAX_PRIORITY); // Prioritize sender thread
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Handle interrupt properly
            e.printStackTrace();
        } finally {
            closeConnection(); // Ensure all resources are closed
        }
    }

    /**
     * Handles sending messages from the server to the client.
     * Continuously reads from the console and sends messages until "exit" is entered.
     */
    public void messageSendingService() throws IOException {
        Scanner readConsole = new Scanner(System.in);
        String consoleData = "";
        System.out.println("Type exit to stop service");

        while (running) {
            consoleData = readConsole.nextLine();
            messageStack.push(consoleData);
            if (Objects.equals(consoleData, "exit")) {
                closeConnection();
                running = false;
                readConsole.close();
                return;
            }

            sendMessage(consoleData); // Send message to client
            consoleData = "";
        }
    }

    /**
     * Sends a message to the client and ensures the message is immediately sent.
     */
    private void sendMessage(String message) {
        messageSender.println(message);
        messageSender.flush();
    }

    /**
     * Continuously receives messages from the client and prints them.
     * Terminates if the server stops running or the connection is lost.
     */
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

    /**
     * Closes all open resources associated with the server and client communication.
     */
    private void closeConnection() {
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
