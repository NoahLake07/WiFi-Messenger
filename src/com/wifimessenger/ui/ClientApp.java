package com.wifimessenger.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.wifimessenger.system.ClientHandler;
import com.wifimessenger.system.Server;
import com.wifimessenger.system.data.Conversation;
import com.wifimessenger.ui.tools.FontGallery;
import com.wifimessenger.ui.tools.UIResource;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.Font.BOLD;

public class ClientApp extends JFrame {

    // * CONSTANTS
    public static Dimension SETUP_WINDOW_SIZE = new Dimension(450, 300);
    public static FontGallery fonts = new FontGallery();

    // * Local Objects
    ClientHandler clientHandler;
    private String clientId, username;
    private String serverIpAddress;
    private boolean isTestInstance = false;

    // * UI PANELS
    JPanel setupPanel, connectPanel; // for user setup
    JSplitPane splitPane; // for app
    ConversationPanel conversationSelectorPanel;
    ChatPanel chatPanel;

    public ClientApp(File json) {
        // todo instantiate via json (clientId, username, ipAddress, pathOfMessageHistory)
    }

    public ClientApp() {
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
        welcomeText.setLayout(new BoxLayout(welcomeText, BoxLayout.Y_AXIS));
        welcomeText.setBorder(BorderFactory.createEmptyBorder(40, 40, 10, 40));

        JLabel welcomeLbl = new JLabel("Welcome to Wifi Messenger");
        welcomeLbl.setFont(FontGallery.getFont("rubik-regular", BOLD, 25));
        welcomeText.add(welcomeLbl);
        welcomeLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel actionLbl = new JLabel("Click the button below to get started.");
        actionLbl.setFont(FontGallery.getFont("rubik-light", BOLD, 15));
        actionLbl.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeText.add(actionLbl);

        JButton startSetup = new JButton("Start Setup");
        actionLbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        welcomeText.add(startSetup);
        startSetup.addActionListener(e -> {
            welcomeText.setVisible(false);
            userSetupPage();
        });

        setupPanel.add(welcomeText);

        this.add(setupPanel);
        this.setVisible(true);
    }

    private void loadApp() {
        this.getContentPane().removeAll();
        this.setSize(600, 450);
        this.setResizable(true);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(150);


        this.setVisible(true);
    }

    private void userSetupPage() {
        Font h1 = FontGallery.getFont("rubik-regular", BOLD, 20);
        setupPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        setupPanel.setLayout(new BoxLayout(setupPanel, BoxLayout.Y_AXIS));

        JPanel header = new JPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT));
        header.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        JLabel headerLbl = new JLabel("App Setup");
        headerLbl.setFont(h1);
        header.add(headerLbl);

        Dimension fieldRestrictions = new Dimension(Short.MAX_VALUE, 30);

        JPanel ipAddress = new JPanel();
        ipAddress.setMaximumSize(fieldRestrictions);
        ipAddress.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel ipAddressLabel = new JLabel("IP Address:");
        JTextField ipAddressField = new JTextField(15);
        ipAddress.add(ipAddressLabel);
        ipAddress.add(ipAddressField);
        JButton useDefault = new JButton("Use Default");
        useDefault.putClientProperty("JButton.buttonType", "roundRect");
        useDefault.addActionListener(l -> {
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

        JPanel actionBar = getSetupFieldsPanel(usernameField, ipAddressField);

        setupPanel.add(header);
        setupPanel.add(ipAddress);
        setupPanel.add(username);
        setupPanel.add(actionBar);
    }

    private JPanel getSetupFieldsPanel(JTextField usernameField, JTextField ipAddressField) {
        JPanel actionBar = new JPanel();
        actionBar.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        JButton cancelSetupBtn = new JButton("Cancel Setup");
        cancelSetupBtn.addActionListener(l -> {
            System.exit(1);
        });
        JButton connectBtn = new JButton("Go");
        connectBtn.addActionListener(l -> {
            this.username = usernameField.getText();
            this.serverIpAddress = ipAddressField.getText();
            this.connectAfterSetup();
        });
        actionBar.add(cancelSetupBtn);
        actionBar.add(connectBtn);
        return actionBar;
    }

    public void setTestInstance(boolean b){ // FOR DEV ONLY
        this.isTestInstance = b;
    }

    public void overrideClientId(String clientId){ // FOR DEV ONLY
        this.clientId = clientId;
        this.setTestInstance(true);
    }

    private void connectAfterSetup() {
        connectPanel = new JPanel();
        connectPanel.setBorder(BorderFactory.createEmptyBorder(100, 40, 50, 20));
        connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.Y_AXIS));

        File loadSignFile = UIResource.getFileFromResource("clientloadingsign.gif");
        ImageIcon loading = new ImageIcon(String.valueOf(loadSignFile));
        ImageIcon imageIcon = new ImageIcon(loading.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
        JLabel loadingIcon = new JLabel("", imageIcon, SwingConstants.CENTER);
        connectPanel.add(loadingIcon, CENTER_ALIGNMENT);
        JLabel statusLabel = new JLabel("Loading...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 25));
        statusLabel.setForeground(new Color(65, 166, 227));
        connectPanel.add(statusLabel, CENTER_ALIGNMENT);

        this.add(connectPanel);
        connectPanel.setVisible(true);
        setupPanel.setVisible(false);

        ExecutorService x = Executors.newCachedThreadPool();
        Runnable load = () -> {
            // * LOAD CLIENT
            statusLabel.setText("Connecting to Server...");

            sleep(1000);

            try {
                clientHandler = new ClientHandler((this.clientId = ClientHandler.createNewClientId()), username, serverIpAddress) {
                    @Override
                    public void error(SocketException e) {
                        showError(e, "The connection to the server was lost.");
                    }
                };
                statusLabel.setText("Starting Client Handler...");
                sleep(1000);
                clientHandler.startListening();
                sleep(1000);
                statusLabel.setText("Creating sources...");
                setupAppDirectory();
                sleep(1000);
                statusLabel.setText("Done!");
                loadingIcon.setVisible(false);
                sleep(1000);
                connectPanel.setVisible(false);
                this.setVisible(false);
                loadApp();
            } catch (IOException e) {
                connectPanel.setVisible(false);
                setupPanel.setVisible(true);
                clientHandlerInstantiationError(e);
            }
        };
        x.submit(load);
    }

    private void setupAppDirectory() {
        File appFolder = new File(System.getProperty("user.home") + "/.wifiMessagingClient/");
        boolean success = appFolder.mkdir();

        if (success || Files.exists(appFolder.toPath())) {
            File conversationFolder = new File(appFolder.getPath() + "/conversations/");
            conversationFolder.mkdir();
            this.clientHandler.setConversationDirectory(conversationFolder);
        } else {
            showError(new IOException("Failed to create app folder via appFolder.mkdir()"),
                    "Failed to create app resource folder at user.home");
            return;
        }

    }

    private void clientHandlerInstantiationError(IOException e) {
        showError(e, "Connection error. Ensure you're connected to the internet " +
                "and the server IP is correct, then try again.");
    }

    private void showError(Exception e, String message) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.setMaximumSize(new Dimension(100, 100));

        JLabel errorLabel = new JLabel(message);
        JButton showDetails = new JButton("Show Error");
        JLabel detailedError = new JLabel(e.getLocalizedMessage());
        detailedError.setForeground(new Color(115, 0, 0));
        detailedError.setVisible(false);

        panel.add(errorLabel);
        panel.add(showDetails);
        panel.add(detailedError);
        showDetails.addActionListener(l -> {
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

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asks the server to fetch the username of the clientID provided.
     *
     * @param clientId the client id to find the name of
     */
    private String getClientNameFromServer(String clientId) {
        if(isTestInstance){
            return clientId;
        } else {
            try {
                return clientHandler.getClientNameFromServer(clientId);
            } catch (IOException e) {
                showError(e, "Failed to fetch conversation info");
            }
        }
        return null;
    }

    private void openConversation(Conversation c) {
        System.out.println("Opened conversation");
    }

    public ConversationPanel getInstanceOfConversationPanel(ArrayList<Conversation> conversations){
        return new ConversationPanel(conversations);
    }

    public class ConversationPanel extends JPanel {
        private ArrayList<Conversation> conversations;
        private JLabel label;
        private JPanel conversationSelector;
        private JScrollPane scrollPane;
        private JButton newConversationBtn;

        public ConversationPanel(ArrayList<Conversation> conversations){
            this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
            this.conversations = conversations;
            scrollPane = new JScrollPane(conversationSelector);
            scrollPane.putClientProperty( "ScrollBar.thumbArc", 999 );
            scrollPane.putClientProperty( "ScrollBar.thumbInsets", new Insets( 2, 2, 2, 2 ) );

            newConversationBtn = new JButton("New Conversation");
            newConversationBtn.putClientProperty( "JButton.buttonType", "roundRect" );
            newConversationBtn.setMaximumSize(new Dimension(Short.MAX_VALUE,100));

            label = new JLabel("Conversations");
            label.setFont(new Font("Arial", BOLD,21));
            label.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            add(label);
            add(newConversationBtn);
            add(scrollPane);

            updatePanel();
        }

        public void updatePanel(){
            conversationSelector = new JPanel();
            conversationSelector.setLayout(new BoxLayout(conversationSelector,BoxLayout.Y_AXIS));

            for(Conversation conversation : conversations) {
                ConversationTile cTile = new ConversationTile(conversation);
                conversationSelector.add(cTile);
            }
        }
    }

    //todo test conversation panel with existing conversation parameters

    public ConversationTile getTestTile() {
        return new ConversationTile("Foreign Client", "This was the last message sent in this conversation.");
    }

    public class ConversationTile extends JPanel {

        JPanel text;
        JLabel name, lastMsg;
        ProfileIcon icon;
        Conversation c;

        public final static Color TILE_BACKGROUND = new Color(187, 187, 187);

        public ConversationTile(Conversation con) {
            super(new FlowLayout(FlowLayout.LEFT));
            this.setMaximumSize(new Dimension(200, 70));
            this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            this.setBackground(TILE_BACKGROUND);

            String foreignClientId = con.getOppositeClientId(clientId);
            String foreignClientName = getClientNameFromServer(foreignClientId);
            icon = new ProfileIcon(foreignClientName);
            add(icon);

            text = new JPanel();
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

            name = new JLabel(foreignClientName);
            name.setFont(new Font("Arial", Font.BOLD, 12));
            lastMsg = new JLabel(con.getLastMessageContent());
            lastMsg.setFont(new Font("Arial", Font.PLAIN, 9));
            lastMsg.setForeground(new Color(61, 61, 61));

            text.add(name);
            text.add(lastMsg);
            add(text);

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    openConversation(c);
                }
            });

            this.putClientProperty( FlatClientProperties.STYLE, "arc: 8" );
        }

        public ConversationTile(String foreignClientName, String lastMsgContent) {
            super(new FlowLayout(FlowLayout.LEFT));
            this.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
            this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            this.setBackground(TILE_BACKGROUND);

            icon = new ProfileIcon(foreignClientName);
            add(icon);

            text = new JPanel();
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.setOpaque(false);

            name = new JLabel(foreignClientName);
            name.setFont(new Font("Arial", Font.BOLD, 12));
            name.setOpaque(false);
            lastMsg = new JLabel(lastMsgContent);
            lastMsg.setFont(new Font("Arial", Font.PLAIN, 9));
            lastMsg.setForeground(new Color(61, 61, 61));
            lastMsg.setOpaque(false);

            text.add(name);
            text.add(lastMsg);
            add(text);
        }
    }

    public class ProfileIcon extends JLabel {
        public static Color INITIAL_COLOR = new Color(0, 0, 0);
        public static Color BORDER_COLOR = new Color(25, 109, 194);
        public static int SIZE = 40;

        public ProfileIcon(String name) {
            super();

            this.setBorder(BorderFactory.createLineBorder(BORDER_COLOR,1));
            this.setForeground(INITIAL_COLOR);
            this.setText(deriveInitials(name));
            this.setFont(new Font("Arial", Font.PLAIN, 10));
            this.setHorizontalAlignment(CENTER);
            this.setMaximumSize(new Dimension(75,75));

            Dimension mySize = new Dimension(SIZE, SIZE);
            this.setMaximumSize(mySize);
            this.setPreferredSize(mySize);
        }

        private String deriveInitials(String name) {
            ArrayList<Character> initials = new ArrayList<>();

            for (int i = 0; i < name.length() - 1; i++) {
                if (name.charAt(i) == ' ') {
                    initials.add(name.charAt(i + 1));
                }
                if (i == 0) {
                    initials.add(name.charAt(i));
                }
            }

            StringBuilder sb = new StringBuilder();
            for (char c : initials) {
                sb.append(c);
            }
            return sb.toString().toUpperCase();
        }
    }

    private class ChatPanel extends JPanel {

    }

    private class InputBar extends JPanel {

    }

    // FOR DEV TESTING ONLY
    public static void main(String[] args) {
            new ClientApp();
        }


}
