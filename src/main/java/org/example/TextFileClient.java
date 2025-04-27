package org.example;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TextFileClient {
    private static final Map<String, Integer> wordCountMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: <server1_host> <server1_port> <server2_host> <server2_port>");
            return;
        }

        String server1Host = args[0];
        int server1Port = Integer.parseInt(args[1]);
        String server2Host = args[2];
        int server2Port = Integer.parseInt(args[3]);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> connectAndRead(server1Host, server1Port));
        executor.submit(() -> connectAndRead(server2Host, server2Port));

        executor.shutdown();

        try {
            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(120, TimeUnit.SECONDS))
                    System.err.println("Executor did not terminate");
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for tasks to finish: " + e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        printTop5Words();
    }

    private static void connectAndRead(String host, int port) {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String line;
            while ((line = in.readLine()) != null) {
                processLine(line);
            }

        } catch (IOException e) {
            System.err.println("Error connecting to server " + host + ":" + port + " - " + e.getMessage());
        }
    }

    private static void processLine(String line) {
        String[] words = line.toLowerCase().split("\\W+");
        for (String word : words) {
            if (!word.isEmpty()) {
                wordCountMap.merge(word, 1, Integer::sum);
            }
        }
    }

    private static void printTop5Words() {
        wordCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .forEach(entry -> System.out.println(entry.getKey() + " - " + entry.getValue()));
    }
}
