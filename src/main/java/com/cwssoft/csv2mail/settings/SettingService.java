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

    public static final String EMAIL_COLUMN = "csv.field.email";
    public static final String EMAIL_SUBJECT = "mail.title";
    public static final String EMAIL_TEXT_BODY = "mail.body.text";
    public static final String EMAIL_QUEUE_SIZE = "mail.queue.maxsize";
    public static final String EMAIL_QUEUE_TIMEOUT = "mail.queue.timeout";
    public static final String EMAIL_CSV_OUTPUT = "csv.output.fields";
    public static final String EMAIL_CSV_OUTPUT_FILENAME = "csv.output.filename";
    public static final String EMAIL_CSV_OUTPUT_DESCRIPTION = "csv.output.description";


    private volatile Properties properties;
    private String path;

    public SettingService(String path) {
        this.path = path;
    }

    public boolean load() {
        synchronized (this) {
            try (InputStream is = new FileInputStream(path)) {
                properties = new Properties();
                properties.load(is);
                return true;
            } catch (IOException ex) {
                logger.warn("Failed to load settings ({}) due to: ", path, ex.getMessage(), ex);
            }
        }
        return false;
    }

    public Optional<String> getSetting(String key) {
        if ( properties == null ) {
            synchronized (this) {
                if ( properties == null ) {
                    load();
                }
            }
        }

        if ( key != null ) {
            return Optional.ofNullable(properties.getProperty(key.trim()));
        }
        return Optional.empty();
    }

}
