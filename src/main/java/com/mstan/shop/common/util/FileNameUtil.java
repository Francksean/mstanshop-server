package com.mstan.shop.common.util;

public final class FileNameUtil {

    private FileNameUtil() {
    }

    public static String sanitize(String filename) {
        if (filename == null || filename.isBlank()) {
            return "image";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
