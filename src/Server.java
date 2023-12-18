import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static String SERVER_IP_ADDRESS = "192.168.7.223";
    public static int PORT = 5000;

    private BufferedReader input;
    private PrintWriter output;

    private ServerSocket serverSocket;
    private ClientMap clientMap;
    private ArrayList<ClientConnection> clientConnections;
    private ConnectionService connectionService;

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
            serverSocket = new ServerSocket(port);
            this.PORT = port;
            this.clientMap = clientMap;
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

    public void startServer(){
        ExecutorService executor1 = Executors.newCachedThreadPool();
        executor1.submit(listenForConnections);

        ExecutorService executor2 = Executors.newCachedThreadPool();
        executor2.submit(enableMessaging);
    }

    public Runnable enableMessaging = new Runnable() {
        @Override
        public void run() {
            connectionService = new ConnectionService();
        }
    };

    public Runnable listenForConnections = new Runnable() {
        @Override
        public void run() {
            while(true){
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewClientConnection(clientSocket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };

    public void exportClientMapAsJSON(String dest) {
        this.clientMap.createJSONFile(dest);
    }

    private void sendRawText(String s){
        output.println(s);
    }

    private void handleNewClientConnection(Socket clientSocket){
        System.out.println("Server: New Connection @ " + clientSocket.getLocalAddress());
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
                        username = data[2];
                    }

                    if(!clientMap.existsInMap(clientID)) // check to see if the client is a new client
                        clientMap.add(new ClientMap.ClientProfile(clientID,username));
                    else {
                        // todo handle existing user (feed in messages that are pending in the server database)
                    }
                    System.out.println("\t> Finished handling client connection -> " + clientMap.getUsername(clientID));
                    break;
                }
            }

        }catch (NullPointerException n){
            throw new Error("The client username submitted by client was null. Client map failed to instantiate.\n\t> " + n.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A service that assists the Server in scanning all client sockets to find incoming messages.
     */
    class ConnectionService {



    }

    /**
     * An object used by the ConnectionService that stores client instance data.
     */
    private class ClientConnection {
        Socket clientSocket;
        BufferedReader input;
        PrintWriter output;

        ClientConnection(Socket clientSocket, BufferedReader input, PrintWriter output){
            this.clientSocket = clientSocket;
            this.input = input;
            this.output = output;
        }
    }

    /**
     * A collection of messages that couldn't be sent are stored until the recipient of the message comes back online.
     */
    private class MessageBox {

    }

}
