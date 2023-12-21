package com.wifimessenger.system;

import com.google.gson.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ClientMap extends ArrayList<ClientMap.ClientProfile> {

    public ClientMap(ArrayList<ClientProfile> profiles){
        super();
        this.addAll(profiles);
    }

    public ClientMap(File jsonFile){
        super();
        if(jsonFile.getName().endsWith(".json")){
            Gson gson = new Gson();
            Path path = jsonFile.toPath();

            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                JsonParser parser = new JsonParser();
                JsonElement tree = parser.parse(reader);

                if (tree.isJsonArray()) {
                    JsonArray array = tree.getAsJsonArray();

                    for (JsonElement element : array) {
                        if (element.isJsonObject()) {
                            JsonObject JSONProfile = element.getAsJsonObject();
                            String clientID = JSONProfile.get("clientID").getAsString();
                            String username = JSONProfile.get("username").getAsString();
                            ClientProfile profile = new ClientProfile(clientID, username);
                            this.add(profile);
                        }
                    }
                } else {
                    throw new RuntimeException("The JSON file does not contain an array of objects.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            IOException e = new IOException("The specified file \'" + jsonFile + "\' is not supported. \nOnly JSON files are supported.");
            throw new RuntimeException(e);
        }
    }

    public ClientMap(){
        this(new ArrayList<ClientProfile>());
    }

    public String getUsername(String clientID){
        for (int i = 0; i < this.size(); i++) {
            if(this.get(i).getClientID().equals(clientID)){
                return this.get(i).username;
            }
        }
        return null;
    }

    public String getClientID(String username){
        for (int i = 0; i < this.size(); i++) {
            if(this.get(i).getUsername().equals(username)){
                return this.get(i).clientID;
            }
        }

        return null;
    }

    public boolean existsInMap(String clientID){
        for (int i = 0; i < this.size(); i++) {
            if(this.get(i).equals(clientID)){
                return true;
            }
        }
        return false;
    }

    public void setUsername(String clientID, String newUsername){
        int indexOfClient = getIndexOf(clientID);
        if(indexOfClient != -1){
            this.get(indexOfClient).setUsername(newUsername);
        } else {
            throw new Error("Client ID was not found.");
        }
    }

    private int getIndexOf(String clientID){
        for (int i = 0; i < this.size(); i++) {
            if(this.get(i).clientID.equals(clientID)){
                return i;
            }
        }
        return -1;
    }

    public File updateJSON(String destDir) { // TODO FIX THIS METHOD: IT DOESN'T CREATE A FILE
        String outputFilePath = destDir + (destDir.contains("profilemap.json") ? "" : "profilemap.json");
        File outputFile = new File(outputFilePath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray jsonArray = new JsonArray();

        for (ClientProfile profile : this) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("clientID", profile.getClientID());
            jsonObject.addProperty("username", profile.getUsername());
            jsonArray.add(jsonObject);
        }

        try {
            String jsonString = gson.toJson(jsonArray);
            Files.writeString(outputFile.toPath(), jsonString,
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to JSON file", e);
        }

        return outputFile;
    }

    public static class ClientProfile {
        private String clientID;
        private String username;

        public ClientProfile(String clientID, String username){
            this.clientID = clientID;
            this.username = username;
        }

        String getUsername(){
            return this.username;
        }

        String getClientID(){
            return this.clientID;
        }

        void setUsername(String s){
            this.username = s;
        }

        @Override
        public boolean equals(Object s){
            return s.equals(this.clientID);
        }
    }

}
