package app;

import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        final String serverAddress = "localhost";
        final int serverPort = 8888;

        try {
            Socket socket = new Socket(serverAddress, serverPort);

            // Create separate threads for reading and writing to the server
            Thread readThread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String serverResponse;
                    while ((serverResponse = reader.readLine()) != null) {
                        System.out.println(serverResponse);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread writeThread = new Thread(() -> {
                try {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                    String userInput;
                    while ((userInput = consoleReader.readLine()) != null) {
                        writer.println(userInput);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            readThread.start();
            writeThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

