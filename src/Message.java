import java.time.LocalDateTime;

public class Message {

    private String sender, messageContent, timestamp, messageId;
    private boolean read = false;
    public static String DELIMITER = "`"; // marks the splits between different data
    public static char MESSAGE_STAMP = '%'; // marks the beginning of a parsed message

    public Message(String sender, String messageContent, String timestamp, String messageId) {
        this.sender = sender;
        this.messageContent = messageContent;
        this.timestamp = timestamp;
        this.messageId = messageId;
    }

    public Message(String parsedMessage){
        Message decoded = Message.decodeParsedMessage(parsedMessage);
        this.sender = decoded.getSender();
        this.messageContent = decoded.getMessageContent();
        this.timestamp = decoded.getTimestamp();
        this.messageId = decoded.getMessageId();
    }

    public Message(){
        this.sender = null;
        this.messageContent = null;
        this.timestamp = null;
        this.messageId = null;
    }

    public String getSender() {
        return sender;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public String getMessageId(){
        return messageId;
    }

    public String getParsedMessage(){
        return MESSAGE_STAMP +
                    sender +            DELIMITER +
                    messageContent +    DELIMITER +
                    timestamp +         DELIMITER +
                    messageId +
                MESSAGE_STAMP;
    }

    public void setTimestampToNow(){
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

        String timestamp = month + "/" + day + "/" + year + " " +
                hour + ":" + (String.valueOf(minute).length() != 2 ? "0" + minute : minute) + " " +
                meridiem;
        this.timestamp = timestamp;
    }

    public boolean isRead(){
        return this.read;
    }

    public void markAsRead(){
        this.read = true;
    }

    public static Message decodeParsedMessage(String parsedMessage){
        String innerPM = parsedMessage.substring(1, parsedMessage.length() - 1); // remove message stamps

        Message m = new Message();
        String[] splitData = innerPM.split(DELIMITER + "");
        m.sender = splitData[0];
        m.messageContent = splitData[1];
        m.timestamp = splitData[2];
        m.messageId = splitData[3];

        return m;
    }

}
