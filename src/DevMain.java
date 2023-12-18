import java.io.File;

public class DevMain {

    public void runtest(){
        Server appServer = new Server(5000,new File("dev\\profilemap.json\\"));
        appServer.startServer();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String clientID = "CLIENT001";
        ClientHandler clientHandler = new ClientHandler(clientID);
        clientHandler.setServerUsername("John B. Doe");

        appServer.exportClientMapAsJSON("dev\\");
    }

    public static void main(String[] args) {
        new DevMain().runtest();
    }

}
