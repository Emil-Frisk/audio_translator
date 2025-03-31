package com.example.application.config;

import java.io.File;

public class AppConfig {
    // The single instance of AppConfig
    private static final AppConfig INSTANCE = new AppConfig();

    // The app root directory
    private final File appRoot;

    // Private constructor to prevent external instantiation
    private AppConfig() {
        this.appRoot = determineAppRoot();
    }

    // Public method to get the singleton instance
    public static AppConfig getInstance() {
        return INSTANCE;
    }

    // Public method to access the app root
    public File getAppRoot() {
        return appRoot;
    }

    private File determineAppRoot() {
        // Check for a configured property first (e.g., -Dmyapp.root=/path)
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