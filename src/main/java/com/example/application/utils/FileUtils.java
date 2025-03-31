package com.example.application.utils;

import java.io.File;

public class FileUtils {
    public static String getExtensionFromMimeType(String mimeType) {
        switch (mimeType) {
            case "audio/mpeg": return ".mp3";
            case "audio/wav": return ".wav";
            case "audio/opus": return ".opus";
            case "audio/ogg": return ".ogg";
            default: return ".unknown";
        }
    }

    public static File getAppRoot() {
        // Check for a configured property first (e.g., set via -Dmyapp.root=/path)
        String configuredRoot = System.getProperty("myapp.root");
        if (configuredRoot != null && !configuredRoot.isEmpty()) {
            return new File(configuredRoot);
        }

        // Try to find the project root by looking for pom.xml or .gitignore
        File currentDir = new File(System.getProperty("user.dir"));
        while (currentDir != null) {
            if (new File(currentDir, "pom.xml").exists() || new File(currentDir, ".gitignore").exists()) {
                return currentDir;
            }
            currentDir = currentDir.getParentFile();
        }

        // Fallback to working directory
        return new File(System.getProperty("user.dir"));
    }
}
