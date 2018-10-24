package com.cwssoft.csv2mail.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class SettingService {

    private static Logger logger = LoggerFactory.getLogger(SettingService.class);

    private Properties properties;
    private String path;

    public SettingService(String path) {
        this.path = path;
    }

    public boolean load() {
        try (InputStream is = new FileInputStream(path)) {
            properties = new Properties();
            properties.load(is);
            return true;
        } catch ( IOException ex ) {
            logger.warn("Failed to load settings ({}) due to: ", path, ex.getMessage(), ex);
        }
        return false;
    }

    public Optional<String> getSetting(String key) {
        if ( key != null ) {
            return Optional.ofNullable(properties.getProperty(key.trim()));
        }
        return Optional.empty();
    }

}
