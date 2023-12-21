package com.wifimessenger.system;

import com.wifimessenger.system.data.MessageStatus;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static String SERVER_IP_ADDRESS = "192.168.7.223"; // todo make this alterable
    public static int PORT = 8080;

    // * MESSAGE I/O
    private BufferedReader input;
    private PrintWriter output;

    // * CONNECTIONS & MAPS
    private ServerSocket serverSocket;
    private ClientMap clientMap;
    private ArrayList<ClientConnection> clientConnections;
    private ConnectionService connectionService;

    // * LOCAL DATA STORAGE
    private String clientMapJsonFileLoc = "/dev/profilemap.json";
    private String messageBoxLoc = null;
    private MessageBox messageBox;

    public Server(){
        this(PORT);
    }

    /**
     * Creates a new Server object with a blank client map and a port given by a parameter.
     * @param port
     */
    public Server(int port){
        this(port,new ClientMap());
    }

    /**
     * Creates a new Server object with a specified client map and a port.
     * @param port The port on which the ServerSocket is instantiated
     * @param clientMap A pre-populated client map
     */
    public Server(int port, ClientMap clientMap){
        try {
            println("Server instantiation started.");
            serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName("0.0.0.0"));
            this.PORT = port;
            this.clientMap = clientMap;
            println("Server instantiated. Port: " + serverSocket.getLocalPort() +"\tInfo: "+serverSocket.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Server object with a specified client map (in a JSON file) and a port.
     * @param port The port on which the ServerSocket is instantiated
     * @param clientMapJSON A pre-populated client map saved in a JSON file
     */
    public Server(int port, File clientMapJSON){
        this(port,new ClientMap(clientMapJSON));
    }

    public void loadMessageBox(MessageBox mb){
        this.messageBox = mb;
    }

    public void loadMessageBox(File mb) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream
                = new FileInputStream("yourfile.txt");
        ObjectInputStream objectInputStream
                = new ObjectInputStream(fileInputStream);
        MessageBox loaded = (MessageBox) objectInputStream.readObject();
        objectInputStream.close();

        loadMessageBox(loaded);
    }

    public void startServer(){
        ExecutorService executor1 = Executors.newCachedThreadPool();
        executor1.submit(listenForConnections);

        ExecutorService executor2 = Executors.newCachedThreadPool();
        executor2.submit(enableMessaging);

        println("Started cached threads...");
    }

    private Runnable enableMessaging = new Runnable() {
        @Override
        public void run() {
            connectionService = new ConnectionService();
        }
    };

    private Runnable listenForConnections = new Runnable() {
        @Override
        public void run() {
            while(true){
                try {
                    println("Waiting to accept a client connection...");
                    Socket clientSocket = serverSocket.accept();
                    println("Client accepted. Processing request...");
                    handleNewClientConnection(clientSocket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };

    private void sendRawText(String s){
        output.println(s);
    }

    public void sendMsg(String msg, String senderID, String recipientID){
        // composite message
        Message m = new Message();
        m.setStatus(MessageStatus.PENDING);
        m.setMessageID(getNewMessageID());
        m.setSenderID(senderID);
        m.setReceiverID(recipientID);
        m.setMessageContent(msg);
        m.setTimestamp();

        // deliver
        connectionService.deliver(m);
    }

    public void handleNewClientConnection(Socket clientSocket){
        println("Server: New Connection @ " + clientSocket.getLocalAddress());

        try{
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            output.println("/requestClientID/");

            String receivedMessage;
            while ((receivedMessage = input.readLine()) != null) {
                if(receivedMessage.contains("/clientID/")){
                    String[] data = receivedMessage.substring(1, receivedMessage.length() - 1).split("/");

                    String clientID = data[1];
                    String username = null;
                    if(receivedMessage.contains("/username/")){
                        username = data[3];
                    }

                    if(!clientMap.existsInMap(clientID))
                        // new client -> add to map
                        clientMap.add(new ClientMap.ClientProfile(clientID,username));
                    else {
                        // returning client -> check for any user updates -> check message box for pending messages for the user
                        String fetchedUsername = clientMap.getUsername(clientID);
                        if(!fetchedUsername.equals(username)){
                            // update with new username
                            clientMap.setUsername(clientID,username);
                            println("\t> Updated username from \"" + fetchedUsername + "\" to \"" + username + "\"");
                        }
                    }
                    println("\t> Finished handling client connection -> " + clientMap.getUsername(clientID));
                    newConnection(clientID,clientMap.getUsername(clientID));
                    updateJSON();
                    break;
                }
            }

        }catch (NullPointerException n){
            throw new Error("The client username submitted by client was null. Client map failed to instantiate.\n\t> " + n.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void newConnection(String id, String username){}

    private String getNewMessageID(){
        return UUID.randomUUID().toString();
    }

    public void println(String s){
        System.out.println(s);
    }

    private void updateJSON(){
        clientMap.updateJSON(this.clientMapJsonFileLoc);
    }

    public String getIpAddress(){
        return SERVER_IP_ADDRESS;
    }

    /**
     * A service that assists the Server in scanning all client sockets to find incoming messages.
     */
    class ConnectionService {

        private ArrayList<ClientConnection> connections;
        private boolean run = false;

          ConnectionService(){
            this(new ArrayList<>());
        }

        ConnectionService(ArrayList<ClientConnection> connections){
            this.connections = connections;
        }

        void startService(){
            ExecutorService serviceThreadPool = Executors.newCachedThreadPool();
            serviceThreadPool.submit(this::startScanning);
        }

        void stop(){
            this.run = false;
        }

        boolean isRunning(){
            return this.run;
        }

        private void startScanning(){
            run = true;
            while(this.run){
                for (ClientConnection connection : this.connections){
                    String receivedMessage;
                    try {
                        if((receivedMessage = connection.input.readLine()) != null)
                            handleIncomingMessage(receivedMessage);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        private void handleIncomingMessage(String receivedInput){
              if(!receivedInput.startsWith("/")){
                  return;
              }

              this.deliver(Message.decodeMessage(receivedInput));
        }

        void deliver(Message m){
              String recipientID = m.getReceiverID();
            for (ClientConnection connection : this.connections){
                if(connection.clientID.equals(recipientID)){
                    // check if the client recipient is active
                    if(connection.isActive()){
                        connection.output.println(m.parse());
                    } else {
                        messageBox.store(m);
                        // TODO send a receipt to the sender (the message wasn't delivered because the client is offline)
                    }
                    break;
                }
            }
        }

    }

    /**
     * An object used by the ConnectionService that stores client instance data.
     */
    private class ClientConnection {
        Socket clientSocket;
        BufferedReader input;
        PrintWriter output;
        String clientID;

        ClientConnection(Socket clientSocket, BufferedReader input, PrintWriter output, String clientID){
            this.clientSocket = clientSocket;
            this.input = input;
            this.output = output;
        }

        boolean isActive(){
            int pingMs = this.pingClient();
            if(pingMs != -1 && pingMs < 1000){
                return true;
            } else {
                if(pingMs==-1){
                    println("Server: client ping was unsuccessful. client is offline.");
                } else {
                    println("Server: Client ping took too long. Client is either offline or their network is too slow.");
                }
                return false;
            }
        }

        int pingClient() {
            long startTime = System.currentTimeMillis();

            try {
                // Ping
                int byteLength = "/ping/".getBytes().length;
                this.output.println("/ping/");

                // Wait for a response
                byte[] response = new byte[byteLength];
                clientSocket.getInputStream().read(response);

                long endTime = System.currentTimeMillis();
                return (int) (endTime - startTime);
            } catch (IOException e) {
                println("Server: " + e.getMessage());
                return -1;
            }
        }

        boolean isConnected(){
            return this.clientSocket.isConnected();
        }
    }

    /**
     * A collection of messages that couldn't be sent are stored until the recipient of the message comes back online.
     */
    private class MessageBox implements Serializable {
        private ArrayList<Message> messages;

        MessageBox(){
            messages = new ArrayList<>();
        }

        int size(){
            return this.messages.size();
        }

        void store(Message m){
            this.messages.add(m);
        }

        boolean clientHasUndeliveredMessages(String recipientClientID){
            for(Message m : messages){
                if(m.getReceiverID().equals(recipientClientID)){
                    return true;
                }
            }
            return false;
        }

        int clientUndeliveredMessageQty(String recipientClientID){
            int qty = 0;
            for(Message m : messages){
                if(m.getReceiverID().equals(recipientClientID)){
                    qty++;
                }
            }
            return qty;
        }

        Message[] getAllMessagesForRecipient(String recipientClientID, boolean removeAfterFetched){
            int qty = this.clientUndeliveredMessageQty(recipientClientID);
            Message[] messages = new Message[qty];

            int tracker = 0;
            for(Message m : messages)
                if (m.getReceiverID().equals(recipientClientID)) {
                    messages[tracker++] = m;
                    if(removeAfterFetched) this.messages.remove(m);
                }

            return messages;
        }
    }

}
