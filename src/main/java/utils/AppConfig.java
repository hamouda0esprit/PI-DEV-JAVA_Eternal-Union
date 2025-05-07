package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application configuration utility
 */
public class AppConfig {
    private static Properties properties;
    private static final String CONFIG_FILE = "config.properties";
    
    // Default values
    private static final String DEFAULT_GEMINI_API_KEY = "YOUR_API_KEY_HERE";
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        properties = new Properties();
        
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Unable to find " + CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("Error loading config file: " + e.getMessage());
        }
    }
    
    /**
     * Get the Gemini API key from config file
     * @return Gemini API key
     */
    public static String getGeminiApiKey() {
        return properties.getProperty("gemini.api.key", DEFAULT_GEMINI_API_KEY);
    }
} 