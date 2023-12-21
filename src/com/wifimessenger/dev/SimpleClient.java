package com.wifimessenger.dev;

import java.io.IOException;
import java.net.Socket;

public class SimpleClient {
    private Socket socket;

    public SimpleClient(String serverIP, int port) {
        try {
            socket = new Socket(serverIP, port);
            System.out.println("Connected to server.");
            // Add code here to send/receive data with the server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            socket.close();
            System.out.println("Client closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SimpleClient client = new SimpleClient("192.168.7.223", 8080); // Replace with the server's IP and port
    }
}
