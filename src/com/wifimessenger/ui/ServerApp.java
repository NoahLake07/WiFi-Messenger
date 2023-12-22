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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ServerApp extends JFrame {

    // * Constants
    public static final int DEFAULT_PORT = 8080;
    public static final Dimension normalAppSize = new Dimension(300,400);
    public static final Dimension debugAppSize = new Dimension(400,533);
    public static final Color SUCCESS_COLOR = new Color(16, 54, 0);

    // * Backend connections
    Server server;

    // * UI Panels
    JPanel header, header2, status, home, detailsPg;
    ClientList clients;
    JLabel statusLbl;
    ConsoleTextArea console;
    JCheckBox debugCheckbox;

    public ServerApp(){
        this(false);
    }

    public ServerApp(boolean debugMode){
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
        debugCheckbox.setSelected(debugMode);
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

        ActionListener l = e -> {
            String serverIpAddress = server.getIpAddress();
            String versionNumber = "1.0";

            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            JLabel ipAddressLabel = new JLabel("Server IP Address: " + serverIpAddress);
            ipAddressLabel.addMouseListener( new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if ( SwingUtilities.isRightMouseButton(e) ) {
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem itemRemove = new JMenuItem("Copy");
                        itemRemove.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                StringSelection selection = new StringSelection(serverIpAddress);
                                clipboard.setContents(selection, null);
                            }
                        });
                        menu.add(itemRemove);
                        menu.show(ipAddressLabel, e.getPoint().x, e.getPoint().y);
                    }
                }
            });
            JLabel versionLabel = new JLabel("App Version: " + versionNumber);

            panel.add(ipAddressLabel);
            panel.add(versionLabel);

            // Show the dialog
            JOptionPane.showMessageDialog(
                    null,
                    panel,
                    "Server Information",
                    JOptionPane.INFORMATION_MESSAGE
            );
        };

        detailsBtn.addActionListener(l);

        Runnable debugRunnable = ()-> {
            boolean b = debugCheckbox.isSelected();
            console.setVisible(b);
            if(b){
                while(getWidth()<=debugAppSize.width){
                    setSize(getWidth()+1,getHeight()+1);
                }
            }else {
                while(getWidth()>=normalAppSize.width){
                    setSize(getWidth()-1,getHeight()-1);
                }
            }
        };
        debugCheckbox.addActionListener(e->{
            debugRunnable.run();
        });
        if(debugMode){
            debugRunnable.run();
        }

        add(home);
        home.setVisible(true);
        this.setVisible(true);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        server = new Server(){
            @Override
            public void newConnection(String id, String username){
                println("New connection established.\n\t-> " + id + "  USERNAME: " + username);
                clients.addClient(getClientString(id,username));
            }

            @Override
            public void println(String s){
                super.println(s);
                if(debugCheckbox.isSelected()){
                    console.append("\n"+s);
                }
            }

            @Override
            public void println(String s, Color c){
                super.println(s);
                if(debugCheckbox.isSelected()){
                    console.append("\n"+s,c);
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
            setupRightClick();

            listTitle = new JLabel();
            updateLabel();

            setLayout(new BorderLayout());
            add(listTitle, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
        }

        private void setupRightClick(){
            clientsList.addMouseListener( new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if ( SwingUtilities.isRightMouseButton(e) ) {
                        clientsList.setSelectedIndex(clientsList.locationToIndex(e.getPoint()));

                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem itemRemove = new JMenuItem("Remove");
                        itemRemove.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                removeClient(clientsList.getSelectedIndex());
                            }
                        });
                        menu.add(itemRemove);
                        menu.show(clientsList, e.getPoint().x, e.getPoint().y);
                    }
                }
            });
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
            if(index!=-1) {
                server.closeClientConnection(clientsModel.get(index));
                clientsModel.removeElementAt(index);
                clientsConnected--;
                updateLabel();
            }
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

            getVerticalScrollBar().setValue(getVerticalScrollBar().getMaximum());
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

    public static void main(String[] args) {
        new ServerApp(true);
    }


}
