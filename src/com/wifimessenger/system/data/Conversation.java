package com.wifimessenger.system.data;

import com.wifimessenger.ui.tools.ObjectSerializer;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;

public class Conversation implements Serializable {

    private ArrayList<Message> messages = null;
    private String[] clientIds = new String[2];
    private boolean isSetup = false;

    public Conversation(){
        this(new ArrayList<>());
        setupClientIds();
    }

    public Conversation(ArrayList<Message> messages){
        this.messages = messages;
        setupClientIds();
    }

    public Conversation(File f) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream
                = new FileInputStream(f);
        ObjectInputStream objectInputStream
                = new ObjectInputStream(fileInputStream);
        Conversation loaded = (Conversation) objectInputStream.readObject();

        this.messages = loaded.messages;
        setupClientIds();
    }

    private void setupClientIds(){
        if(!messages.isEmpty() && !isSetup){
            clientIds[0] = messages.get(0).getSenderID();
            clientIds[1] = messages.get(0).getReceiverID();
            isSetup = true;
        }
    }

    public void add(Message m){
        this.messages.add(m);
        setupClientIds();
    }

    public ArrayList<Message> getMessages(){
        return this.messages;
    }

    public String getLastMessageContent(){
        Message m = getLastMessage();
        if(m!=null){
            return m.getMessageContent();
        }
        return "";
    }

    public Message getLastMessage(){
        if(!messages.isEmpty()){
            return this.messages.get(messages.size()-1);
        }
        return null;
    }

    public void serializeTo(Path path) throws IOException {
        new ObjectSerializer<>().serialize(this,path);
    }

    public boolean hasClient(String clientId){
        if(isSetup)
            return clientIds[0].equals(clientId) || clientIds[1].equals(clientId);
        return false;
    }

    public String getOppositeClientId(String clientId){
        if(clientIds[0].equals(clientId)){
            return clientIds[1];
        } else {
            return clientIds[0];
        }
    }

}
