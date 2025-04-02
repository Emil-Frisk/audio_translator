package com.example.application.config;

import java.io.File;

import com.example.application.exceptions.FileNotFound;

public class AppConfig {
    // The single instance of AppConfig
    private static final AppConfig INSTANCE = new AppConfig();

    // The app root directory
    private final File appRoot;
    private final String pythonExe;

    // Private constructor to prevent external instantiation
    private AppConfig() {
        this.appRoot = determineAppRoot();
        this.pythonExe = determinePythonExe();
    }

    // Public method to get the singleton instance
    public static AppConfig getInstance() {
        return INSTANCE;
    }

    // Public method to access the app root
    public File getAppRoot() {
        return appRoot;
    }

    public String getPythonExe() {
        return pythonExe;
    }

    private String determinePythonExe() {
        String osName = System.getProperty("os.name").toLowerCase();
        String pythonExecutable = null;

        // Developement python location
        if (osName.contains("win")) { 
            pythonExecutable = new File("C:\\koulu\\vaadin\\vaadin-lopputyo\\.venv\\Scripts\\python.exe").getAbsolutePath();
        } else {// linux location
            pythonExecutable = new File("python").getAbsolutePath();
        }

        if (!new File(pythonExecutable).exists()) {
            throw new FileNotFound("Python executable not found in virtual environment: " + pythonExecutable);
        }

        return pythonExecutable;
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