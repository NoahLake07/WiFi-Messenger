package com.wifimessenger.system;

import com.wifimessenger.system.data.MessageStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {

    private String senderID;
    private String receiverID;
    private String messageContent;
    private String timestamp;
    private String messageID;
    private MessageStatus status;

    void setSenderID(String senderID){
        this.senderID = senderID;
    }

    void setReceiverID(String receiverID){
        this.receiverID = receiverID;
    }

    void setMessageContent(String messageContent){
        this.messageContent = messageContent;
    }

    void setTimestamp(String timestamp){
        this.timestamp = timestamp;
    }

    public void setTimestamp(){
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

    void setMessageID(String messageID){
        this.messageID = messageID;
    }

    void setStatus(MessageStatus status){
        this.status = status;
    }

    public String getSenderID() {
        return this.senderID;
    }

    public String getReceiverID(){
        return this.receiverID;
    }

    public String getMessageContent(){
        return this.messageContent;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

    public String getMessageID(){
        return this.messageID;
    }

    public static Message decodeMessage(String parsedMessage){
        String[] data = parsedMessage.substring(1, parsedMessage.length() - 1).split("/");
        Message m = new Message();
        m.setMessageID(data[0]);
        m.setSenderID(data[1]);
        m.setReceiverID(data[2]);
        m.setMessageContent(data[3]);
        m.setTimestamp(data[4]);

        MessageStatus status;
        if(data[5].toLowerCase().equals("pending")){
            status = MessageStatus.PENDING;
        } else if (data[5].toLowerCase().equals("delivered")){
            status = MessageStatus.DELIVERED;
        } else {
            status = MessageStatus.READ;
        }
        m.setStatus(status);

        return m;
    }

    public String parse(){
        StringBuffer sb = new StringBuffer();
        sb.append("msg/");
        sb.append(messageID).append("/");
        sb.append(senderID).append("/");
        sb.append(receiverID).append("/");
        sb.append(messageContent).append("/");
        sb.append(timestamp).append("/");
        sb.append(status).append("/");
        return sb.toString();
    }

}
