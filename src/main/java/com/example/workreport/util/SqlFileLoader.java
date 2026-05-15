package com.example.workreport.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class SqlFileLoader {

    private SqlFileLoader() {
    }

    public static String load(String path) {
        InputStream inputStream = SqlFileLoader.class.getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("SQL file not found: " + path);
        }

        try {
            return read(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read SQL file: " + path, e);
        }
    }

    private static String read(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toString("UTF-8");
        } finally {
            inputStream.close();
        }
    }
}
