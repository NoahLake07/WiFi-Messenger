package com.wifimessenger.ui;

import com.wifimessenger.system.ClientHandler;

import javax.swing.*;

public class ClientApp extends JFrame {

    ClientHandler clientHandler;
    private String clientId;
    private String serverIpAddress;

    // * UI PANELS
    JPanel setupPanel;

    public ClientApp(String clientId, String username, String ipAddress){
        this.clientId = clientId;
        this.serverIpAddress = ipAddress;

        this.clientHandler = new ClientHandler(clientId,username,ipAddress);
        this.clientHandler.startListening();
    }

    void clientUserSetup(){
        // todo have the user create their username and connect to the server ip address
    }

}
