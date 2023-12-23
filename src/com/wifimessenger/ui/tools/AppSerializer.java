package com.wifimessenger.ui.tools;

import java.io.*;
import java.nio.file.Path;

public class AppSerializer<T> {

    public void serialize(T object, Path path) throws IOException {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            outputStream.writeObject(object);
        }
    }

    public T load(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (T) inputStream.readObject();
        }
    }

}
