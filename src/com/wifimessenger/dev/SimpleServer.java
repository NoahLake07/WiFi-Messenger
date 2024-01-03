package com.wifimessenger.dev;

import java.io.*;
import java.net.*;

public class SimpleServer {
    private ServerSocket serverSocket;

    public SimpleServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started. Waiting for clients...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());\

                // Handle client connection (add code here to read/write to the client)
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            serverSocket.close();
            System.out.println("Server closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SimpleServer server = new SimpleServer(8080);
        server.start();
    }
}
