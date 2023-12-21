import com.wifimessenger.dev.TCPClient;
import com.wifimessenger.dev.TCPServer;
import com.wifimessenger.system.ClientHandler;
import com.wifimessenger.system.Server;
import com.wifimessenger.ui.ClientApp;
import com.wifimessenger.ui.ServerApp;

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
        ClientHandler clientHandler = new ClientHandler(clientID,c1_username,Server.SERVER_IP_ADDRESS);
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
            ClientHandler clientHandler = new ClientHandler(clientID,c1_username,Server.SERVER_IP_ADDRESS);
        }
    }

    public void runServer(){
        ServerApp serverApp = new ServerApp();
    }

    public void connectToServerViaClient(){
        ClientHandler ch = new ClientHandler("clientidxxx","John Smith",Server.SERVER_IP_ADDRESS);
        ch.startListening();
    }

    public void runAppTest(){
        new DevMain().runServer();
    }

    public void runSimpleServer(){
        try {
            TCPServer x = new TCPServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void runSimpleClient(){
        try {
            TCPClient x = new TCPClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new DevMain().connectToServerViaClient();
    }

}
