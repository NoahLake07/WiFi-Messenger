package com.wifimessenger.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.wifimessenger.system.ClientHandler;
import com.wifimessenger.system.Server;
import com.wifimessenger.ui.tools.FontGallery;
import com.wifimessenger.ui.tools.UIResource;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.Font.BOLD;

public class ClientApp extends JFrame {

    // * CONSTANTS
    public static Dimension SETUP_WINDOW_SIZE = new Dimension(450,300);
    public static FontGallery fonts = new FontGallery();

    // * Local Objects
    ClientHandler clientHandler;
    private String clientId, username;
    private String serverIpAddress;

    // * UI PANELS
    JPanel setupPanel, connectPanel; // for user setup

    public ClientApp(File json){
        // todo instantiate via json (clientId, username, ipAddress, pathOfMessageHistory)
    }

    public ClientApp(){
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        this.setTitle("Wifi Messenger Setup");
        this.setupPanel = new JPanel();
        this.setSize(SETUP_WINDOW_SIZE);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel welcomeText = new JPanel();
        welcomeText.setLayout(new BoxLayout(welcomeText,BoxLayout.Y_AXIS));
        welcomeText.setBorder(BorderFactory.createEmptyBorder(40,40,10,40));

            JLabel welcomeLbl = new JLabel("Welcome to Wifi Messenger");
            welcomeLbl.setFont(FontGallery.getFont("rubik-regular",BOLD,25));
            welcomeText.add(welcomeLbl);
            welcomeLbl.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel actionLbl = new JLabel("Click the button below to get started.");
            actionLbl.setFont(FontGallery.getFont("rubik-light ", BOLD,15));
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

    private void loadApp(){
        // todo load client home
    }

    private void userSetupPage(){
        Font h1 = FontGallery.getFont("rubik-regular",BOLD,20);
        setupPanel.setBorder(BorderFactory.createEmptyBorder(20,40,40,40));
        setupPanel.setLayout(new BoxLayout(setupPanel,BoxLayout.Y_AXIS));

        JPanel header = new JPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT));
        header.setMaximumSize(new Dimension(Short.MAX_VALUE,40));
            JLabel headerLbl = new JLabel("App Setup");
            headerLbl.setFont(h1);
            header.add(headerLbl);

        Dimension fieldRestrictions = new Dimension(Short.MAX_VALUE,30);

        JPanel ipAddress = new JPanel();
        ipAddress.setMaximumSize(fieldRestrictions);
        ipAddress.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel ipAddressLabel = new JLabel("IP Address:");
            JTextField ipAddressField = new JTextField(15);
            ipAddress.add(ipAddressLabel);
            ipAddress.add(ipAddressField);
            JButton useDefault = new JButton("Use Default");
            useDefault.putClientProperty( "JButton.buttonType", "roundRect" );
            useDefault.addActionListener(l->{
                ipAddressField.setText(Server.SERVER_IP_ADDRESS);
            });
            ipAddress.add(useDefault);

        JPanel username = new JPanel();
        username.setMaximumSize(fieldRestrictions);
        username.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel usernameLabel = new JLabel("Username: ");
            JTextField usernameField = new JTextField(15);
            username.add(usernameLabel);
            username.add(usernameField);

        JPanel actionBar = new JPanel();
        actionBar.setBorder(BorderFactory.createEmptyBorder(30,0,30,0));
            JButton cancelSetupBtn = new JButton("Cancel Setup");
            cancelSetupBtn.addActionListener(l->{
                System.exit(1);
            });
            JButton connectBtn = new JButton("Go");
            connectBtn.addActionListener(l->{
                this.username = usernameField.getText();
                this.serverIpAddress = ipAddressField.getText();
                this.connectAfterSetup();
            });
            actionBar.add(cancelSetupBtn);
            actionBar.add(connectBtn);

        setupPanel.add(header);
        setupPanel.add(ipAddress);
        setupPanel.add(username);
        setupPanel.add(actionBar);
    }

    private void connectAfterSetup(){
        connectPanel = new JPanel();
        connectPanel.setBorder(BorderFactory.createEmptyBorder(100,40,50,20));
        connectPanel.setLayout(new BoxLayout(connectPanel,BoxLayout.Y_AXIS));

        File loadSignFile = UIResource.getFileFromResource("clientloadingsign.gif");
        ImageIcon loading = new ImageIcon(String.valueOf(loadSignFile));
        ImageIcon imageIcon = new ImageIcon(loading.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
        JLabel loadingIcon = new JLabel("",imageIcon,SwingConstants.CENTER);
        connectPanel.add(loadingIcon,CENTER_ALIGNMENT);
        JLabel statusLabel = new JLabel("Loading...");
        statusLabel.setFont(new Font("Segoe UI",Font.PLAIN,25));
        statusLabel.setForeground(new Color(65, 166, 227));
        connectPanel.add(statusLabel,CENTER_ALIGNMENT);

        this.add(connectPanel);
        connectPanel.setVisible(true);
        setupPanel.setVisible(false);

        ExecutorService x = Executors.newCachedThreadPool();
        Runnable load = ()->{
            // * LOAD CLIENT
            statusLabel.setText("Connecting to Server...");

            sleep(1000);

            try {
                clientHandler = new ClientHandler((this.clientId = ClientHandler.createNewClientId()), username, serverIpAddress){
                    @Override
                    public void error(SocketException e){
                        showError(e, "The connection to the server was lost.");
                    }
                };
                statusLabel.setText("Starting Client Handler...");
                sleep(1000);
                clientHandler.startListening();
                sleep(1000);
                statusLabel.setText("Creating sources...");

                // todo create a directory for saved data
                // todo save data via json (clientId, username, ipAddress, pathOfMessageHistory)

                loadingIcon.setVisible(false);
                sleep(1000);
                connectPanel.setVisible(false);
                loadApp();
            } catch (IOException e) {
                connectPanel.setVisible(false);
                setupPanel.setVisible(true);
                clientHandlerInstantiationError(e);
            }
        };
        x.submit(load);
    }

    private void clientHandlerInstantiationError(IOException e){
        showError(e,"Connection error. Ensure you're connected to the internet " +
                "and the server IP is correct, then try again.");
    }

    private void showError(Exception e, String message){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.setMaximumSize(new Dimension(100,100));

        JLabel errorLabel = new JLabel(message);
        JButton showDetails = new JButton("Show Error");
        JLabel detailedError = new JLabel(e.getLocalizedMessage());
        detailedError.setForeground(new Color(115, 0, 0));
        detailedError.setVisible(false);

        panel.add(errorLabel);
        panel.add(showDetails);
        panel.add(detailedError);
        showDetails.addActionListener(l->{
            showDetails.setVisible(false);
            detailedError.setVisible(true);
        });

        // Show the dialog
        JOptionPane.showMessageDialog(
                null,
                panel,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // FOR DEV TESTING ONLY
    public static void main(String[] args) {
        new ClientApp();
    }

}
