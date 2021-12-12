package com.example.bot.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotProperties {
    private static Properties prop;

    private static void load() throws IOException {
        InputStream input = new FileInputStream("src/main/resources/config.properties");
        prop = new Properties();
        prop.load(input);
    }

    public static String getProperty(String key) {
        if (prop == null) {
            try {
                load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String props = prop.getProperty(key);
        if (props == null || props.equals("system")) {
            props = System.getenv(key);
        }
        return props;
    }
}
