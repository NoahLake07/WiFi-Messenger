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

    public static Color FOREIGN_COLOR = new Color(101, 61, 0);
    public static Color MY_COLOR = new Color(88, 0, 134);

    private int PORT;
    private String IP_ADDRESS;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private int myCode;
    private Role role;

    private ConsoleTextArea console;
    private JScrollPane scrollPane;

    public MessageAppInstance(int port, String ipAddress, String windowTitle, Role role) {
        // * UI SETUP
        super();
        this.setSize(450, 450);
        this.setTitle(windowTitle);
        scrollPane = new JScrollPane();
        //scrollPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
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
        this.setVisible(true);
        console.append("Instance running...\n",Color.BLACK);

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

        this.console.append("Messenger App Instance Setup Complete.\n", new Color(74, 180, 0));
    }

    public MessageAppInstance(int port, String windowTitle){
        this(port,getIpAddress(),windowTitle, Role.CLIENT);
    }

    public MessageAppInstance(int port, String windowTitle, Role role){
        this(port, getIpAddress(), windowTitle, role);
    }

    public void setRole(MessageAppInstance.Role role){
        this.role = role;
    }

    private void setupUniqueCode(){
        Random random = new Random();
        this.myCode = 100_000_000 + random.nextInt(900_000_000);
    }

    private void serverConnectionSetup() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.PORT);
            this.console.append("Server is running and waiting for connections...\n", new Color(74, 180, 0));

            ExecutorService executor = Executors.newCachedThreadPool(); // Use a thread pool
            while (true) {
                Socket clientSocket = serverSocket.accept();
                this.console.append("Client connected: " + clientSocket.getInetAddress().getHostAddress() + "\n", new Color(74, 180, 0));

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
        if(messageReceived.startsWith("%")){
            // todo decode message
        } else {
            console.append("Anonymous: ", FOREIGN_COLOR);
            console.append(messageReceived + "\n", Color.BLACK);
        }
    }

    public void sendMessage(String messageToSend) {
        // Send message to the other instance
        output.println(messageToSend);

        // display on UI
        console.append("ME: ", MY_COLOR);
        console.append(messageToSend + "\n", Color.BLACK);
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

    enum Role {
        SERVER, CLIENT
    }

}
