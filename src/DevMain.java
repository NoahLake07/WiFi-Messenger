import com.wifimessenger.dev.TCPClient;
import com.wifimessenger.dev.TCPServer;
import com.wifimessenger.system.ClientHandler;
import com.wifimessenger.system.Server;
import com.wifimessenger.ui.ClientApp;
import com.wifimessenger.ui.ServerApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

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

    public void runSimpleServer() throws IOException {
        System.out.println("server is started");
        ServerSocket serverSocket= new ServerSocket(55286);
        System.out.println("server is waiting");
        Socket socket=serverSocket.accept();
        System.out.println("Client connected");
        BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String str=reader.readLine();
        System.out.println("Client data: "+str);
        socket.close();
        serverSocket.close();
    }

    public void runSimpleClient() throws IOException {
        Socket socket = new Socket("127.0.0.1", 55286);
        OutputStreamWriter os = new OutputStreamWriter(socket.getOutputStream());
        os.write("Santosh Karna");
        os.flush();
        socket.close();
    }

    public static void main(String[] args) {
        try {
            new DevMain().runSimpleServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
