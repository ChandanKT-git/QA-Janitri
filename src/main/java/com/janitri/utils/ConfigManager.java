package com.janitri.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration manager to handle test properties
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_CONFIG_FILE = "default_config.properties";
    private static Properties properties;
    private static ConfigManager instance;

    private ConfigManager() {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Get singleton instance of ConfigManager
     * @return ConfigManager instance
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Load properties from config file
     */
    private void loadProperties() {
        try {
            // First try to load from user-defined config file
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                    properties.load(input);
                }
            } else {
                // If user-defined config doesn't exist, load default config
                Path defaultConfigPath = Paths.get("src", "main", "resources", DEFAULT_CONFIG_FILE);
                if (Files.exists(defaultConfigPath)) {
                    try (InputStream input = new FileInputStream(defaultConfigPath.toString())) {
                        properties.load(input);
                    }
                } else {
                    System.err.println("No configuration file found. Using hardcoded defaults.");
                    setDefaultProperties();
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            setDefaultProperties();
        }
    }

    /**
     * Set default properties if no config file is found
     */
    private void setDefaultProperties() {
        properties.setProperty("browser", "chrome");
        properties.setProperty("baseUrl", "https://dev-dash.janitri.in/");
        properties.setProperty("timeout", "10");
        properties.setProperty("headless", "false");
        properties.setProperty("takeScreenshotOnFailure", "true");
    }

    /**
     * Get property value
     * @param key Property key
     * @return Property value or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get property value with default
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default value if not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get integer property value
     * @param key Property key
     * @param defaultValue Default value if property not found or not an integer
     * @return Property value as integer or default value
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get boolean property value
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value as boolean or default value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}