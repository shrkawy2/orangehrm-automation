package com.orangehrm.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {

    private static final Properties PROPERTIES = new Properties();
    private static boolean loaded = false;

    private ConfigReader() {
    }

    private static synchronized void load() {
        if (loaded) return;
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found on classpath");
            }
            PROPERTIES.load(input);
            loaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        load();
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null) {
            throw new RuntimeException("Missing config key: " + key);
        }
        return value;
    }

    public static String getBaseUrl() { return get("base.url"); }
    public static String getUsername() { return get("username"); }
    public static String getPassword() { return get("password"); }
    public static String getBrowser() { return get("browser"); }
    public static boolean isHeadless() { return Boolean.parseBoolean(get("headless")); }
    public static int getImplicitWaitSeconds() { return Integer.parseInt(get("implicit.wait.seconds")); }
    public static int getExplicitWaitSeconds() { return Integer.parseInt(get("explicit.wait.seconds")); }
}
