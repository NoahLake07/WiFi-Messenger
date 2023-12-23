package com.wifimessenger.system.data;

import com.wifimessenger.system.Server;
import com.wifimessenger.ui.tools.AppSerializer;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;

public class Conversation implements Serializable {

    private ArrayList<Message> messages = null;

    public Conversation(){
        this(new ArrayList<>());
    }

    public Conversation(ArrayList<Message> messages){
        this.messages = messages;
    }

    public Conversation(File f) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream
                = new FileInputStream(f);
        ObjectInputStream objectInputStream
                = new ObjectInputStream(fileInputStream);
        Conversation loaded = (Conversation) objectInputStream.readObject();

        this.messages = loaded.messages;
    }

    public void add(Message m){
        this.messages.add(m);
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
        new AppSerializer<>().serialize(this,path);
    }

}
