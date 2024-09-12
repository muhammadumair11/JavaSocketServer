package org.javasocketserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket socket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;


    public Server(int port) throws IOException {
        socket = new ServerSocket(port);
        System.out.println("Creater Running on PORT:    " + port);

        clientSocket = socket.accept();
        System.out.println("Client connected");

        InputStream inputStream = clientSocket.getInputStream();
        in = new BufferedReader(new InputStreamReader(inputStream));
        out = new PrintWriter(clientSocket.getOutputStream(), true);


        out.println("a test message");

        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Received from client: " + message);
        }
    }

    private void stopConnection() throws IOException {
        socket.close();
    }

    
}
