package com.wifimessenger.ui.tools;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class FontGallery {

    public FontGallery(){
        searchAndLoadFonts(Objects.requireNonNull(UIResource.getFileFromResource("fonts")));
    }

    private static void searchAndLoadFonts(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively search within subdirectories
                    searchAndLoadFonts(file);
                } else if (file.isFile() && file.getName().endsWith(".ttf")) {
                    // Load font files
                    try {
                        String fontName = file.getName().substring(0, file.getName().lastIndexOf('.')).toLowerCase();
                        Font font = Font.createFont(Font.TRUETYPE_FONT, file).deriveFont(12f); // Change the font size as needed
                        putFont(fontName, font);
                    } catch (IOException | FontFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static HashMap<String,Font> fontMap = new HashMap<>();

    public static void putFont(String fontName,Font f){
        fontMap.put(fontName,f);
    }

    public static Font getFont(String name, int style, int size) {
        // Get the font from the map and return that font in the specified style/size
        Font selectedFont = fontMap.get(name.toLowerCase());
        if (selectedFont != null) {
            return selectedFont.deriveFont(style, size);
        } else {
            // Return a default font or handle the missing font case
            System.err.println("Couldn't find font type " + name);
            return new Font("Arial", style, size);
        }
    }

    public static Font getFont(String name, int size){
        return getFont(name,Font.PLAIN,size);
    }

    public Font getFontFromFile(String fontFilePath, int size) throws IOException, FontFormatException {
        return Font.createFont(Font.TRUETYPE_FONT, new File(fontFilePath)).deriveFont(size);
    }

    public Font registerFont(String filePath, float size) throws IOException, FontFormatException {
        Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File(filePath)).deriveFont(size);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(customFont);
        return customFont;
    }

}
