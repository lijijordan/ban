package com.jordan.ban.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.logging.log4j.core.util.Loader.getClassLoader;

public class AppKeyLoader {

    private static final Map<String, String> KEYS = new HashMap<>();
    private static Properties prop = new Properties();

    static {
        loadKeys();
    }

    public static String getKey(String key) {
        return KEYS.get(key.toLowerCase());
    }

    public static void main(String[] args) {
//        System.out.println(getKey("Huobi.key_id"));

        String DISPATCHER_SERVLET_PROPERTIES_PKG = "config.properties";
        URL url = null;
        ClassLoader loader = AppKeyLoader.class.getClassLoader();
        url = loader.getResource(DISPATCHER_SERVLET_PROPERTIES_PKG);
        System.out.println(url);
        url = ClassLoader.getSystemResource(DISPATCHER_SERVLET_PROPERTIES_PKG);
        System.out.println(url);
    }

    public static void loadKeys() {

        InputStream input = null;
        try {
            String filename = "config.properties";
            if (input == null) {
                System.out.println("Sorry, unable to find " + filename);
                return;
            }
            prop.load(AppKeyLoader.class.getResourceAsStream("config.properties"));
            Enumeration<?> e = prop.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = prop.getProperty(key);
                System.out.println("Key : " + key + ", Value : " + value);
                KEYS.put(key, value);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
