public class Main {

    public static void main(String[] args) {
        int PORT = 5000;

        // Launch server instance (i1) in a separate thread
        new Thread(() -> {
            System.out.println("Attempting to launch instance 1...");
            MessageAppInstance i1 = new MessageAppInstance(PORT, "Instance 1", MessageAppInstance.Role.SERVER);
        }).start();

        // Launch client instance (i2)
        System.out.println("Attempting to launch instance 2...");
        MessageAppInstance i2 = new MessageAppInstance(PORT, "Instance 2", MessageAppInstance.Role.CLIENT);
    }



}
