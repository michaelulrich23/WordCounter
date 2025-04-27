package org.example;
import java.io.*;
import java.net.*;

public class Server {
    private final int port;
    private final String filePath;

    public Server(int port, String filePath) {
        this.port = port;
        this.filePath = filePath;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Waiting for client on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept(); BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                    System.out.println("Client connected.");

                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        out.println(line);
                    }

                    System.out.println("Finished sending file: " + filePath);

                } catch (IOException e) {
                    System.err.println("Error during file reading or sending: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: <port> <file_path>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String filePath = args[1];

        Server server = new Server(port, filePath);
        server.start();
    }
}
