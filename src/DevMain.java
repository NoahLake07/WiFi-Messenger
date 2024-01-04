import com.wifimessenger.system.ClientHandler;
import com.wifimessenger.system.Server;
import com.wifimessenger.system.data.Conversation;
import com.wifimessenger.system.data.Message;
import com.wifimessenger.ui.ClientApp.ConversationPanel;
import com.wifimessenger.ui.ClientApp.ProfileIcon;
import com.wifimessenger.ui.ClientApp;
import com.wifimessenger.ui.ServerApp;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class DevMain {

    public void runtest(){
        Server appServer = new Server(5000);
        appServer.startServer();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String clientID = "CLIENT001", c1_username = "John B. Doe";
        try {
            ClientHandler clientHandler = new ClientHandler(clientID,c1_username,Server.SERVER_IP_ADDRESS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void runClientThresholdTest(int qty){
        ServerApp server = new ServerApp();

        for (int i = 0; i < qty; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            String clientID = ClientHandler.createNewClientId(), c1_username = "CLIENT00"+(i+1);
            try {
                ClientHandler clientHandler = new ClientHandler(clientID,c1_username,Server.SERVER_IP_ADDRESS);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void runServer(){
        ServerApp serverApp = new ServerApp(true);
    }

    public void connectToServerViaClient(){
        ClientHandler ch = null;
        try {
            ch = new ClientHandler("clientidxxx","John Smith", Server.SERVER_IP_ADDRESS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ch.startListening();
    }

    public void testProfileIcon(){
        JFrame frame = new JFrame();
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));
        panel.add(new ClientApp().getTestTile());
        panel.setMaximumSize(new Dimension(200,100));
        frame.setSize(400,400);
        frame.add(panel);

        frame.setVisible(true);
    }

    public void testConversationPanel(){
        // * INSTANTIATE TEST CONVERSATIONS
        ArrayList<Conversation> conversations = new ArrayList<>();
        conversations.add(buildConversationA());
        conversations.add(buildConversationB());

        // * INSTANTIATE UI
        JFrame frame = new JFrame();
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));

        ClientApp cA = new ClientApp();
        cA.overrideClientId("mySenderId01");
        cA.setTestInstance(true);
        cA.setVisible(false);
        ConversationPanel cP = cA.getInstanceOfConversationPanel(conversations);

        panel.add(cP);
        panel.setMaximumSize(new Dimension(600,600));
        frame.setSize(600,600);
        frame.add(panel);

        frame.setVisible(true);
    }

    public void testConversationTile(){
        // * INSTANTIATE TEST CONVERSATIONS
        Conversation c = buildConversationA();

        // * INSTANTIATE UI
        JFrame frame = new JFrame();
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));

        ClientApp cA = new ClientApp();
        cA.overrideClientId("mySenderId01");
        cA.setTestInstance(true);
        cA.setVisible(false);
        ClientApp.ConversationTile cT = cA.getTestTile();

        panel.add(cT);
        panel.setMaximumSize(new Dimension(600,600));
        frame.setSize(600,600);
        frame.add(panel);

        frame.setVisible(true);
    }

    public Message buildMessage(String senderID,String receiverID, String messageContent, String messageID){
        Message m = new Message();
        m.setSenderID(senderID);
        m.setReceiverID(receiverID);
        m.setMessageContent(messageContent);
        m.setTimestamp();
        m.setMessageID(messageID);
        return m;
    }

    public Conversation buildConversationA(){
        String foreignSenderId = "fsi01";
        String localSenderId = "mySenderId01";
        Conversation c = new Conversation();
        Message m1 = buildMessage(localSenderId,foreignSenderId,"This is message A.","m1a");
        Message m2 = buildMessage(foreignSenderId,localSenderId,"This is message B.","m2a");
        Message m3 = buildMessage(localSenderId,foreignSenderId,"This is message C.","m3a");

        c.add(m1);
        c.add(m2);
        c.add(m3);
        return c;
    }

    public Conversation buildConversationB(){
        String foreignSenderId = "fsi02";
        String localSenderId = "mySenderId01";
        Conversation c = new Conversation();
        Message m1 = buildMessage(localSenderId,foreignSenderId,"This is message A.","m1b");
        Message m2 = buildMessage(foreignSenderId,localSenderId,"This is message B.","m2b");
        Message m3 = buildMessage(localSenderId,foreignSenderId,"This is message C.","m3b");

        c.add(m1);
        c.add(m2);
        c.add(m3);
        return c;
    }

    public static void main(String[] args) {
        new DevMain().runServer();
    }

}
