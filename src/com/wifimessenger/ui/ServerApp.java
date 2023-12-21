package com.wifimessenger.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.wifimessenger.system.Server;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class ServerApp extends JFrame {

    // * Constants
    public static final int DEFAULT_PORT = 8080;
    public static final Dimension normalAppSize = new Dimension(300,400);
    public static final Dimension debugAppSize = new Dimension(400,533);

    // * Backend connections
    Server server;

    // * UI Panels
    JPanel header, header2, status, home, detailsPg;
    ClientList clients;
    JLabel statusLbl;
    ConsoleTextArea console;
    JCheckBox debugCheckbox;

    public ServerApp(){
        super();
        this.setSize(normalAppSize);
        this.setResizable(false);
        this.setTitle("Server");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        home = new JPanel();
        detailsPg = new JPanel();
        home.setLayout(new BoxLayout(home,BoxLayout.Y_AXIS));

        header = new JPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel title = new JLabel("WiFi Message Server");
        title.setFont(new Font("Arial",Font.BOLD,20));
        header.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));
        header.setBackground(new Color(229, 229, 229));
        header.add(title);
        header.setMaximumSize(new Dimension(Short.MAX_VALUE,60));

        status = new JPanel();
        status.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel("Status: ");
        status.add(lbl);
        statusLbl = new JLabel();
        lbl.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
        status.add(lbl);
        status.add(statusLbl);
        setStatus(Status.LAUNCHING);
        status.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        JPanel debug = new JPanel();
        debug.setLayout(new FlowLayout(FlowLayout.LEFT));
        debug.setBorder(BorderFactory.createEmptyBorder(0,25,0,0));

        debugCheckbox = new JCheckBox("Enable Debug Console");
        debugCheckbox.addActionListener(e->{
            boolean b = debugCheckbox.isSelected();
            console.setVisible(b);
            if(b){
                while(this.getWidth()<=debugAppSize.width){
                    this.setSize(this.getWidth()+1,this.getHeight()+1);
                }
            }else {
                while(this.getWidth()>=normalAppSize.width){
                    this.setSize(this.getWidth()-1,this.getHeight()-1);
                }
            }
        });
        debugCheckbox.setSelected(false);
        debug.add(debugCheckbox);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(0,25,0,0));
        JButton detailsBtn = new JButton("Server Details");
        detailsPanel.add(detailsBtn);

        console = new ConsoleTextArea();
        console.clear();

        clients = new ClientList();

        clients.setPreferredSize(new Dimension(Short.MAX_VALUE,100));
        console.setPreferredSize(new Dimension(debugCheckbox.getPreferredSize().width,100));

        home.add(header);
        home.add(status);
        home.add(debug);
        home.add(detailsBtn);
        home.add(clients);
        home.add(console);
        console.setVisible(false);

        detailsBtn.addActionListener(e->{
            // Input values (server IP address and version number)
            String serverIpAddress = server.getIpAddress(); // Replace with your server IP address
            String versionNumber = "1.0"; // Replace with your version number

            // Create a JPanel for the server information
            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            JLabel ipAddressLabel = new JLabel("Server IP Address: " + serverIpAddress);
            JLabel versionLabel = new JLabel("Version Number: " + versionNumber);

            panel.add(ipAddressLabel);
            panel.add(versionLabel);

            // Show the dialog
            JOptionPane.showMessageDialog(
                    null,
                    panel,
                    "Server Information",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        add(home);
        home.setVisible(true);
        this.setVisible(true);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        server = new Server(DEFAULT_PORT){
            @Override
            public void newConnection(String id, String username){
                clients.addClient(getClientString(id,username));
            }

            @Override
            public void println(String s){
                if(debugCheckbox.isSelected()){
                    console.append(s + "\n");
                }
            }
        };
        server.startServer();
        setStatus(Status.ACTIVE);
    }

    String getClientString(String id, String username){
        return username.toUpperCase();
    }

    void setStatus(Status status){
        statusLbl.setText(status.toString().toUpperCase());

        switch (status.toString().toLowerCase()){
            case "launching":
                statusLbl.setForeground(new Color(40, 64, 94));
                break;
            case "active":
                statusLbl.setForeground(new Color(89, 154, 28));
                break;
            case "error":
                statusLbl.setForeground(new Color(94, 29, 29));
                break;
        }
    }

    enum Status {
        LAUNCHING,
        ERROR,
        ACTIVE,
        OFF
    }

    public class ClientList extends JPanel {
        private DefaultListModel<String> clientsModel;
        private JList<String> clientsList;
        private int clientsConnected = 0;
        private JLabel listTitle;

        public ClientList() {
            this.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            clientsModel = new DefaultListModel<>();
            clientsList = new JList<>(clientsModel);
            clientsList.setDragEnabled(false);
            clientsList.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
            clientsList.setForeground(new Color(0, 44, 79));
            JScrollPane scrollPane = new JScrollPane(clientsList);

            listTitle = new JLabel();
            updateLabel();

            setLayout(new BorderLayout());
            add(listTitle, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
        }

        private void updateLabel(){
            listTitle.setText("Connected Clients (" + clientsConnected + ")");
        }

        public void addClient(String clientName) {
            clientsModel.addElement(clientName);
            clientsConnected++;
            updateLabel();
        }

        public void removeClient(int index) {
            clientsModel.removeElementAt(index);
            clientsConnected--;
            updateLabel();
        }

        public int getSelectedIndex() {
            return clientsList.getSelectedIndex();
        }

        public String getSelectedClient() {
            return clientsList.getSelectedValue();
        }

        public void clearClients() {
            clientsModel.clear();
            clientsConnected = 0;
            updateLabel();
        }
    }

    public class ConsoleTextArea extends JScrollPane {
        private JTextPane textPane;
        private Color defaultColor;
        static final Color DEFAULT_CONSOLE_TEXT_COLOR = new Color(0, 18, 47);

        public ConsoleTextArea() {
            this(300, 200);
        }

        public ConsoleTextArea(int w, int h) {
            textPane = new JTextPane();
            textPane.setEditable(false);

            this.defaultColor = DEFAULT_CONSOLE_TEXT_COLOR;
            textPane.setPreferredSize(new Dimension(w, h));

            setViewportView(textPane);
        }

        public void append(String text, Color color) {
            StyledDocument doc = textPane.getStyledDocument();
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setForeground(style, color);

            try {
                doc.insertString(doc.getLength(), text, style);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void append(String text) {
            append(text, this.defaultColor);
        }

        public void clear() {
            textPane.setText("");
        }

        public void setDefaultColor(Color color) {
            this.defaultColor = color;
        }
    }

}
