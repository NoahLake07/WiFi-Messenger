public class Message {

    private String sender, messageContent, timestamp, messageId;
    public static char DELIMITER = '|'; // marks the splits between different data
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

    public static Message decodeParsedMessage(String parsedMessage){
        String innerPM = parsedMessage.substring(1, parsedMessage.length() - 1);

        Message m = new Message();
        String[] splitData = innerPM.split(DELIMITER + "");
        m.sender = splitData[0];
        m.messageContent = splitData[1];
        m.timestamp = splitData[2];
        m.messageId = splitData[3];

        return m;
    }

}
