import com.wifimessenger.system.ClientHandler;
import com.wifimessenger.system.Server;
import com.wifimessenger.ui.ClientApp.ProfileIcon;
import com.wifimessenger.ui.ClientApp;
import com.wifimessenger.ui.ServerApp;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

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

    public static void main(String[] args) {
        new DevMain().testProfileIcon();
    }

}
