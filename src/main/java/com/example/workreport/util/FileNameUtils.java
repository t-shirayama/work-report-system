package com.example.workreport.util;

public final class FileNameUtils {

    private static final int MAX_NAME_PART_LENGTH = 80;

    private FileNameUtils() {
    }

    public static String sanitizeNamePart(String value, String defaultValue) {
        String sanitized = value == null ? "" : value.trim();
        sanitized = sanitized.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        sanitized = sanitized.replace("..", "_");
        sanitized = sanitized.replaceAll("_+", "_");

        if (sanitized.length() > MAX_NAME_PART_LENGTH) {
            sanitized = sanitized.substring(0, MAX_NAME_PART_LENGTH);
        }

        if (sanitized.length() == 0) {
            return defaultValue;
        }
        return sanitized;
    }

    public static boolean isSafeFileName(String fileName) {
        if (fileName == null || fileName.trim().length() == 0) {
            return false;
        }
        return fileName.indexOf('/') < 0
                && fileName.indexOf('\\') < 0
                && fileName.indexOf(':') < 0
                && !fileName.contains("..");
    }
}
