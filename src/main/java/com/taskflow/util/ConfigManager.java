package com.taskflow.util;

import java.io.*;
import java.util.*;

/**
 * Configuration manager - Singleton pattern (poorly implemented)
 * 
 * Loads config from properties files and environment variables
 * FIXME: Race condition in getInstance() under concurrent access
 */
public class ConfigManager {
    
    private static ConfigManager instance;
    private Properties config;
    private long lastLoadTime;
    
    // FIXME: Not thread-safe singleton
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    private ConfigManager() {
        config = new Properties();
        loadConfig();
    }
    
    private void loadConfig() {
        try {
            // Try to load from classpath
            InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties");
            if (is != null) {
                config.load(is);
                // BUG: InputStream never closed
            }
            
            // Override with environment variables
            String dbUrl = System.getenv("DATABASE_URL");
            if (dbUrl != null) config.setProperty("spring.datasource.url", dbUrl);
            
            String dbUser = System.getenv("DATABASE_USER");
            if (dbUser != null) config.setProperty("spring.datasource.username", dbUser);
            
            String dbPass = System.getenv("DATABASE_PASSWORD");
            if (dbPass != null) config.setProperty("spring.datasource.password", dbPass);
            
            lastLoadTime = System.currentTimeMillis();
            
        } catch (IOException e) {
            // Silently swallow the exception
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }
    
    public String get(String key) {
        return config.getProperty(key);
    }
    
    public String get(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    public int getInt(String key, int defaultValue) {
        String value = config.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = config.getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Reload config every 5 minutes
     * FIXME: Not actually called anywhere, manual reload only
     */
    public void reloadIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastLoadTime > 300000) { // magic number: 5 min in ms
            loadConfig();
        }
    }
    
    /**
     * Dump all config to stdout - used for debugging
     * SECURITY: This dumps passwords and secrets too!
     */
    public void dumpConfig() {
        System.out.println("=== Current Configuration ===");
        for (String key : config.stringPropertyNames()) {
            System.out.println(key + " = " + config.getProperty(key));
        }
        System.out.println("=============================");
    }
    
    // FIXME: No way to reset for testing
}
