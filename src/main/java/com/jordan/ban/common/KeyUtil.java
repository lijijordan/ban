package com.jordan.ban.common;

import com.jordan.ban.market.parser.Fcoin;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.security.KeyException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class KeyUtil {

    private static Map<String, String> keys;

    private static final String FILE_PATH = "/home/liji/kkk";

    public static String getKey(String platform, String keyType) throws KeyException {
        if (keys == null || keys.isEmpty()) {
            keys = load(FILE_PATH);
        }
        return keys.get(platform + "." + keyType);
    }


    public static Map<String, String> load(String filePath) throws KeyException {
        Properties prop = new Properties();
        InputStream input = null;
        HashMap<String, String> hm = new HashMap<String, String>();
        try {
            input = new FileInputStream(filePath);

            // load a properties file
            prop.load(input);

            Enumeration<Object> e = prop.keys();
            while (e.hasMoreElements()) {
                String s = (String) e.nextElement();
                hm.put(s, prop.getProperty(s));
            }
            return hm;
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
        if (hm.isEmpty()) {
            throw new KeyException("Can not load keys");
        }
        return null;
    }

    public static void save(String filePath, Map<String, String> map) {
        if (map.isEmpty()) {
            log.info("Map is empty!");
            return;
        }
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            map.forEach((k, v) -> {
                prop.setProperty(k, v);
            });
            output = new FileOutputStream(filePath);
            // set the properties value

            // save properties to project root folder
            prop.store(output, "keys");
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws KeyException {
        log.info(load(FILE_PATH).toString());
        System.out.println(getKey(Fcoin.PLATFORM_NAME, "key"));
    }
}
