import com.wifimessenger.data.MessageStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    private String clientID;
    private String username; // only used upon instantiation
    private BufferedReader input;
    private PrintWriter output;
    private UUID identificationMaker;
    private boolean serverConnected = false;

    Socket socket;

    public ClientHandler(String clientID) {
        try {
            socket = new Socket(Server.SERVER_IP_ADDRESS, Server.PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            this.clientID = clientID;
            this.startListening();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public ClientHandler(){
        this(createNewClientId());
    }

    public void startListening(){
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable clientListen = () -> {
            try {
                String receivedMessage;
                while ((receivedMessage = input.readLine()) != null) {
                    inputReceived(receivedMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        executor.submit(clientListen);
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
    }

    public void connectToServerViaClientID(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        output.println("/clientID/" + this.clientID + "/username/" + this.username);
        serverConnected = true;
    }

    public void setServerUsername(String newUsername){
        output.println("/newClientUsername/" + clientID + "/" + newUsername + "/"); // todo have the server recognize this
    }

    public String getClientID(){
        return this.clientID;
    }

    public static String createNewClientId(){
        return "-insert new id here-";
    }

    String getNewUniqueID(){
        identificationMaker = UUID.randomUUID();
        return identificationMaker.toString();
    }

}
