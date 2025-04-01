package com.example.application.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public static List<String> deleteFiles(File... files) {
        List<String> failedDeletions = new ArrayList<>();

        // Iterate over each file in the varargs array
        for (File file : files) {
            if (file == null) {
                continue; // Skip null files
            }

            try {
                // Check if the file exists
                if (file.exists()) {
                    // Attempt to delete the file
                    if (file.delete()) {
                        System.out.println("Deleted file: " + file.getAbsolutePath());
                    } else {
                        // File exists but couldn't be deleted
                        failedDeletions.add(file.getAbsolutePath());
                        System.err.println("Failed to delete file: " + file.getAbsolutePath());
                    }
                } else {
                    System.out.println("File does not exist, skipping: " + file.getAbsolutePath());
                }
            } catch (SecurityException e) {
                // Handle security exceptions (e.g., no permission to delete)
                failedDeletions.add(file.getAbsolutePath());
                System.err.println("Security exception while deleting file " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }

        return failedDeletions;
    }

}
