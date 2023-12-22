package com.wifimessenger.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.wifimessenger.system.ClientHandler;

import javax.swing.*;
import java.awt.*;

public class ClientApp extends JFrame {

    // * CONSTANTS
    public static Dimension DEFAULT_APP_SIZE = new Dimension(500,300);

    // * Local Objects
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

    public ClientApp(){
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        this.setTitle("Wifi Messenger Setup");
        this.setupPanel = new JPanel();
        this.setSize(DEFAULT_APP_SIZE);

        JPanel welcomeText = new JPanel();
        welcomeText.setLayout(new BoxLayout(welcomeText,BoxLayout.Y_AXIS));
        welcomeText.setBorder(BorderFactory.createEmptyBorder(40,20,10,20));

            JLabel welcomeLbl = new JLabel("Welcome to Wifi Messenger");
            welcomeLbl.setFont(new Font("Arial",Font.BOLD,20));
            welcomeText.add(welcomeLbl);
            welcomeLbl.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel actionLbl = new JLabel("Welcome to Wifi Messenger");
            actionLbl.setFont(new Font("Arial",Font.PLAIN,12));
            actionLbl.setHorizontalAlignment(SwingConstants.CENTER);
            welcomeText.add(actionLbl);

            JButton startSetup = new JButton("Start Setup");
            actionLbl.setBorder(BorderFactory.createEmptyBorder(10,0,20,0));
            welcomeText.add(startSetup);
            startSetup.addActionListener(e->{
                welcomeText.setVisible(false);
                userSetupPage();
            });

        setupPanel.add(welcomeText);

        this.add(setupPanel);
        this.setVisible(true);
    }

    private void userSetupPage(){
        Font h1 = new Font("Arial",Font.BOLD,21);
        setupPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        setupPanel.setLayout(new BoxLayout(setupPanel,BoxLayout.Y_AXIS));

        JPanel header = new JPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel headerLbl = new JLabel("App Setup");
            headerLbl.setFont(h1);
            header.add(headerLbl);

        JPanel ipAddress = new JPanel();
            JLabel ipAddressLabel = new JLabel("IP Address:");
            JTextField ipAddressField = new JTextField(15);
            ipAddress.add(ipAddressLabel);
            ipAddress.add(ipAddressField);

        JPanel username = new JPanel();
            JLabel usernameLabel = new JLabel("Username:");
            JTextField usernameField = new JTextField(15);
            username.add(usernameLabel);
            username.add(usernameField);

        // TODO add buttions and actionlisteners to complete setup

        setupPanel.add(header);
        setupPanel.add(ipAddress);
        setupPanel.add(username);
    }

    // FOR DEV TESTING ONLY
    public static void main(String[] args) {
        new ClientApp();
    }

}
