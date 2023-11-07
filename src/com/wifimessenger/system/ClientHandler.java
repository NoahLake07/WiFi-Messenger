package com.wifimessenger.system;

import com.wifimessenger.system.data.Conversation;
import com.wifimessenger.system.data.Message;
import com.wifimessenger.system.data.MessageStatus;
import com.wifimessenger.ui.tools.ObjectSerializer;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    private String clientID;
    private String username; // only used upon instantiation
    private BufferedReader input;
    private PrintWriter output;
    private UUID identificationMaker;
    private ArrayList<Conversation> conversations;
    private boolean serverConnected = false;
    private File conversationDirectory;
    private boolean read = true;

    Socket socket;

    public ClientHandler(String clientID, String username, String ipAddress) throws IOException {
        socket = new Socket(ipAddress, Server.PORT);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        this.clientID = clientID;
        this.username = username;
        this.startListening();
    }

    public void startListening(){
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable clientListen = () -> {
            try {
                String receivedMessage;
                while(true) {
                    if(read){
                        if((receivedMessage = input.readLine()) != null){
                            inputReceived(receivedMessage);
                        }
                    }
                }
            } catch (SocketException e) {
                error(e);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        executor.submit(clientListen);
    }

    public void error(SocketException e){
        e.printStackTrace();
    }

    private void inputReceived(String receivedMessage){
        HashMap<String, Runnable> actions = new HashMap<>();

        actions.put("/requestClientID/", this::connectToServerViaClientID);

        for (String key : actions.keySet()) {
            if (receivedMessage.startsWith(key)) {
                Runnable action = actions.get(key);
                action.run();
                break;
            }
        }
    }

    public void sendMessage(String msg, String recipientClientId){
        Message m = new Message();
        m.setMessageContent(msg);
        m.setStatus(MessageStatus.PENDING);
        m.setSenderID(this.clientID);
        m.setReceiverID(recipientClientId);
        m.setTimestamp();
        m.setMessageID(getNewUniqueID());

        output.println(m.parse());
        for(Conversation c : conversations){
            if(c.hasClient(recipientClientId)){
                c.add(m);
            }
        }
    }

    public String getClientNameFromServer(String clientId) throws IOException {
        read = false;

        String parsedRequest = "/request/name/" + clientId + "/" + this.clientID + "/";
        output.println();

        String received = "";
        String retrievedData = null;
        ArrayList<String> unprocessedMessages = new ArrayList<>();
        while((received = input.readLine()) != null){
            if(received.contains("name") && received.contains(clientId)){
                // decode data sent from server
                String[] data = received.substring(1, received.length() - 1).split("/");
                retrievedData = data[1];
                break;
            } else {
                // it wasn't the data being looked for -> add it to a list to decode later
                unprocessedMessages.add(received);
            }
        }

        read = true;

        if(!unprocessedMessages.isEmpty()){
            for(String msg : unprocessedMessages){
                inputReceived(msg);
            }
        }

        return retrievedData;
    }

    public void setConversationDirectory(File f){
        this.conversationDirectory = f;
        updateConversationsFromFile();
    }

    private void saveConversationsToFile() throws IOException {
        if(conversationDirectory.exists()){
            // delete old files
            File[] oldFiles = conversationDirectory.listFiles();
            if (oldFiles != null) {
                for (File oldFile : oldFiles) {
                    Files.deleteIfExists(oldFile.toPath());
                }
            }

            // create new files
            if(!this.conversations.isEmpty()) {
                ObjectSerializer<Conversation> objectSerializer = new ObjectSerializer<>();
                int i = 0;
                for (Conversation conversation : this.conversations) {
                    File convFile = new File(conversationDirectory.getPath() + "/conversation" + i);
                    objectSerializer.serialize(conversation, convFile.toPath());
                    i++;
                }
            }
        }
    }

    private void updateConversationsFromFile() {
        ArrayList<Conversation> conversations = new ArrayList<>();

        ObjectSerializer<Conversation> objSer = new ObjectSerializer<>();
        int conversationQty = Objects.requireNonNull(conversationDirectory.listFiles()).length;
        if(conversationQty>0){
            File[] conversationFiles = conversationDirectory.listFiles();
            for (int i = 0; i < conversationQty; i++) {
                try {
                    assert conversationFiles != null;
                    conversations.add(objSer.load(conversationFiles[i].toPath()));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        this.conversations = conversations;
    }

    void connectToServerViaClientID(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        output.println("/clientID/" + this.clientID + "/username/" + this.username + "/");
        serverConnected = true;
    }

    public void setServerUsername(String newUsername){
        output.println("/newClientUsername/" + clientID + "/" + newUsername + "/");
    }

    public String getClientID(){
        return this.clientID;
    }

    public static String createNewClientId(){
        return UUID.randomUUID().toString();
    }

    String getNewUniqueID(){
        identificationMaker = UUID.randomUUID();
        return identificationMaker.toString();
    }

    public static void main(String[] args) {
        try {
            new ClientHandler("id02","Bill B. Joe",Server.SERVER_IP_ADDRESS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
