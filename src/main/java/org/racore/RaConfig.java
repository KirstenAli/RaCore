package org.racore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RaConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = RaConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                System.out.println("Configuration loaded from " + CONFIG_FILE);
            } else {
                System.out.println("No configuration file found; using default settings.");
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }

    public static int getServerPort() {
        String port = properties.getProperty("server.port", "8080"); // Default port is 8080
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number in config; using default port 8080.");
            return 8080;
        }
    }
}
