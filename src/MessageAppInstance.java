import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MessageAppInstance extends JFrame {

    public static Color FOREIGN_COLOR = new Color(157, 126, 0);
    public static Color ENHANCED_FOREIGN_COLOR = new Color(0, 80, 136);
    public static Color MY_COLOR = new Color(121, 0, 182);
    public static Color TIME_COLOR = new Color(121, 121, 121);
    public static Color SYSTEM_COLOR = new Color(19, 72, 0);

    private String username = "User";
    private boolean enhancedMessagingEnabled = true;
    private int lastMessageId = 0;

    private int PORT;
    private String IP_ADDRESS;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private int myCode;
    private Role role;

    private ConsoleTextArea console;
    private JScrollPane scrollPane;
    private JMenuBar menuBar;

    public MessageAppInstance(int port, String ipAddress, String windowTitle, Role role) {
        // * UI SETUP
        super();
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        this.setSize(450, 450);
        this.setTitle(windowTitle);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        scrollPane = new JScrollPane();
        console = new ConsoleTextArea();
        console.setDefaultColor(Color.BLACK);
        JScrollPane consolePanel = new JScrollPane(console);

        JPanel inputBar = new JPanel();
        inputBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        JTextField input = new JTextField();
        input.setPreferredSize(new Dimension(200,input.getPreferredSize().height));
        JButton sendBtn = new JButton("Send");
        inputBar.add(input);
        inputBar.add(sendBtn);
        inputBar.putClientProperty("Component.focusWidth",2);
        sendBtn.addActionListener(e->{
            String message = input.getText();
            input.setText("");
            if(!message.isEmpty()){
                sendMessage(message);
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(consolePanel);
        splitPane.setBottomComponent(inputBar);
        this.add(splitPane);
        splitPane.setDividerLocation(this.getHeight()-90);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(0);
        console.setFocusable(false);
        this.setVisible(true);
        console.append("Instance running...\n",Color.BLACK);

        setupMenuBar();

        // * MESSENGER SETUP
        this.role = role;
        this.PORT = port;
        this.IP_ADDRESS = ipAddress;
        this.setupUniqueCode();
        console.append((role==Role.SERVER ? "Server instance running- IP: " + this.IP_ADDRESS + "  Port: " + port + "\n": "Client instance running on Port " + port + "\n"));
        if(this.role == Role.SERVER){
            this.serverConnectionSetup();
        } else {
            this.clientConnectionSetup();
        }

        this.console.append("Instance Setup Complete.\n=============================\n", new Color(49, 122, 0));
    }

    public MessageAppInstance(int port, String windowTitle){
        this(port,getIpAddress(),windowTitle, Role.CLIENT);
    }

    public MessageAppInstance(int port, String windowTitle, Role role){
        this(port, getIpAddress(), windowTitle, role);
    }

    private void setupMenuBar(){
        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu settingsMenu = new JMenu("Settings");
        menuBar.add(settingsMenu);

        JMenu networkDetails = new JMenu("Network");
        settingsMenu.add(networkDetails);
            JMenuItem viewDetails = new JMenuItem("View Network Details");
            viewDetails.addActionListener(e->{
                JFrame frame = new JFrame("Network Properties");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(300, 150);
                frame.setLayout(new GridLayout(3, 1));

                JLabel ipLabel = new JLabel("IP Address: " + IP_ADDRESS);
                JLabel portLabel = new JLabel("Port: " + PORT);
                JLabel networkLabel = new JLabel("Network Name: " + getNetworkName());
                ipLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                portLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                networkLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                frame.add(networkLabel);
                frame.add(ipLabel);
                frame.add(portLabel);
                frame.setVisible(true);
            });
            networkDetails.add(viewDetails);
            JMenuItem changePort = new JMenuItem("Change Port...");
            changePort.addActionListener(e->{
                JTextField portField = new JTextField();
                JPanel panel = new JPanel(new GridLayout(2, 1));
                panel.add(new JLabel("Enter New Port:"));
                panel.add(portField);

                int result = JOptionPane.showConfirmDialog(null, panel,
                        "Change Port", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String inputPort = portField.getText();
                    try {
                        int newPort = Integer.parseInt(inputPort);
                        if (isValidPort(newPort)) {
                            PORT = newPort;
                            console.append("The port for this instance has been set to " + newPort + "\n",SYSTEM_COLOR);
                        } else {
                            JOptionPane.showMessageDialog(null, "Invalid port number!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid integer port number!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            networkDetails.add(changePort);

        JMenu userSettings = new JMenu("User");
        settingsMenu.add(userSettings);

        JMenu messageMode = new JMenu("Message Mode");
            ButtonGroup messageGroup = new ButtonGroup();
            JRadioButtonMenuItem basicText = new JRadioButtonMenuItem("Basic Messaging");
            JRadioButtonMenuItem enhancedMessaging = new JRadioButtonMenuItem("Enhanced Messaging");
            enhancedMessaging.setSelected(enhancedMessagingEnabled);
            basicText.setSelected(!enhancedMessagingEnabled);
            enhancedMessaging.addActionListener(e-> setEnhancedMessaging(true));
            basicText.addActionListener(e-> setEnhancedMessaging(false));
            messageGroup.add(basicText);
            messageGroup.add(enhancedMessaging);
            messageMode.add(basicText);
            messageMode.add(enhancedMessaging);

        userSettings.add(messageMode);
        userSettings.addSeparator();
        JMenuItem changeUsername = new JMenuItem("Change Username");
        changeUsername.addActionListener(e->{
            JTextField usernameField = new JTextField(15);

            // Panel to hold the text field
            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adding borders

            // Add the text field to the panel
            panel.add(usernameField);

            // Show the dialog box
            int result = JOptionPane.showConfirmDialog(null, panel,
                    "Enter New Username", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            // Check if OK button is clicked
            if (result == JOptionPane.OK_OPTION) {
                String newUsername = usernameField.getText();
                this.username = newUsername;
                console.append("This device's username has been changed to \"" + newUsername + "\"\n", SYSTEM_COLOR);
            }
        });
        userSettings.add(changeUsername);
        userSettings.add(changeUsername);
        userSettings.add(messageMode);
    }

    private void setEnhancedMessaging(boolean enabled){
        this.enhancedMessagingEnabled = enabled;
    }

    public void setRole(MessageAppInstance.Role role){
        this.role = role;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    private void setupUniqueCode(){
        Random random = new Random();
        this.myCode = 100_000_000 + random.nextInt(900_000_000);
    }

    private void serverConnectionSetup() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.PORT);
            this.console.append("Server is running and waiting for connections...\n", new Color(26, 61, 0));

            ExecutorService executor = Executors.newCachedThreadPool(); // Use a thread pool
            while (true) {
                Socket clientSocket = serverSocket.accept();
                this.console.append("> Client connected: " + clientSocket.getInetAddress().getHostAddress() + "\n", new Color(0, 105, 180));

                // Handle each client connection in a separate thread
                Runnable serverListen = () -> {
                    try {
                        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        output = new PrintWriter(clientSocket.getOutputStream(), true);
                        String receivedMessage;
                        while ((receivedMessage = input.readLine()) != null) {
                            messageReceived(receivedMessage); // Process received messages
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                executor.submit(serverListen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server Connection Established");
    }


    private void clientConnectionSetup() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            ExecutorService executor = Executors.newFixedThreadPool(1);
            Runnable clientListen = () -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = input.readLine()) != null) {
                        messageReceived(receivedMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            executor.submit(clientListen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void messageReceived(String messageReceived) {
        if(messageReceived.startsWith(String.valueOf(Message.MESSAGE_STAMP))){
            Message decodedMessage = new Message(messageReceived);
            appendMessage(decodedMessage,true);
            lastMessageId++;
        } else {
            console.append("Anonymous: ", FOREIGN_COLOR);
            console.append(messageReceived + "\n", Color.BLACK);
        }
    }

    public void appendMessage(Message m, boolean isForeign){
        console.append(m.getTimestamp() + "  ", TIME_COLOR, new Font("Arial",Font.ITALIC,10));
        console.append(m.getSender() + ": ", (isForeign ? ENHANCED_FOREIGN_COLOR : MY_COLOR));
        console.append(m.getMessageContent() + "\n", Color.BLACK);
    }

    public void sendMessage(String messageToSend) {
        if(enhancedMessagingEnabled){
            String mID = "x" + lastMessageId++;
            Message parsedMessage = new Message(this.username,messageToSend,null,mID);
            parsedMessage.setTimestampToNow();
            appendMessage(parsedMessage,false);
            output.println(parsedMessage.getParsedMessage());
        } else {
            // Send message to the other instance
            output.println(messageToSend);

            // display on UI
            console.append("ME: ", MY_COLOR);
            console.append(messageToSend + "\n", Color.BLACK);
        }
    }

    public static String getIpAddress(){
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String ipAddress = localhost.getHostAddress();
        return ipAddress;
    }

    private static String getNetworkName() {
        String networkName = "";
        try {
            InetAddress address = InetAddress.getLocalHost();
            networkName = address.getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return networkName;
    }

    private static boolean isValidPort(int port) {
        return port > 0 && port <= 65535; // Port range is 1 to 65535
    }

    public enum Role {
        SERVER, CLIENT
    }

}
