import java.time.LocalDateTime;

public class Main {

    public static void testFunctionality(){
        int PORT = 5000;

        // Launch server instance (i1) in a separate thread
        new Thread(() -> {
            System.out.println("Attempting to launch instance 1...");
            MessageAppInstance i1 = new MessageAppInstance(PORT, "Server", MessageAppInstance.Role.SERVER);
            i1.setUsername("Server");
        }).start();

        // Launch client instance (i2)
        System.out.println("Attempting to launch instance 2...");
        MessageAppInstance i2 = new MessageAppInstance(PORT, "Instance 2", MessageAppInstance.Role.CLIENT);
        i2.setUsername("Client");
        i2.setTitle("Client");
    }

    public static void testTimestamp(){
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonth().getValue();
        int day = now.getDayOfMonth();
        int year = now.getYear();

        int hour = now.getHour();
        int minute = now.getMinute();

        String meridiem; // 12-hour clock
        if(hour+1 >= 12){
            meridiem = "PM";
            hour-=12;
        } else {
            meridiem = "AM";
        }

        String timestamp = month + "/" + day + "/" + year + "  " + hour + ":" + minute + " " + meridiem;
        System.out.println(timestamp);
    }

    public static void main(String[] args) {
        testFunctionality();
    }

}
