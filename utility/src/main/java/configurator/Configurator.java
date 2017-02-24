package configurator;

import java.io.*;
import java.util.Properties;

/**
 * Singleton class to read the configuration file.
 */
public class Configurator {

    private static Configurator instance = null;
    private static final String configFile = "settings.properties";
    private Properties properties;

    public static Configurator getInstance() {
        if(instance == null) {
            instance = new Configurator();
        }
        return instance;
    }

    private Configurator() {
        try {
            InputStream input = new FileInputStream(configFile);
            properties = new Properties();
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}
