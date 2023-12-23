package com.wifimessenger.ui.tools;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class UIResource {

    public static File getFileFromResource(String path){
        File file;
         URL url = FontGallery.class.getClassLoader().getResource(path);
         if(url != null){ // load for jar
             try {
                 file = new File(url.toURI());
             } catch (URISyntaxException e) {
                 e.printStackTrace();
                 return null;
             }
         } else { // load for IDE
             file = new File("res/"+path);
         }
         return file;
    }

}
