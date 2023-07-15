package me.hsgamer.blockutil.abstraction;

import java.util.Map;
import java.util.Properties;

public final class BlockHandlerSettings {
    private static final Properties PROPERTIES = new Properties();

    private BlockHandlerSettings() {
        // EMPTY
    }

    public static void addAll(Map<String, String> settings) {
        PROPERTIES.putAll(settings);
    }

    public static void set(String key, String value) {
        PROPERTIES.setProperty(key, value);
    }

    public static String get(String key, String def) {
        return PROPERTIES.getProperty(key, def);
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(get(key, Boolean.toString(def)));
    }
}
