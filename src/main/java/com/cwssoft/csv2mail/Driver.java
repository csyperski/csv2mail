package com.cwssoft.csv2mail;

import com.cwssoft.csv2mail.settings.DefaultMailSettingProvider;
import com.cwssoft.csv2mail.settings.SettingService;
import com.cwssoft.mail.DefaultMailManager;
import com.cwssoft.mail.MailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Driver {

    public static final Logger logger = LoggerFactory.getLogger(Driver.class);

    public static void main(String[] args) {
        new Driver(args);
    }

    public Driver(String[] args) {
        logger.debug("Starting csv2email...");
        start(args);
    }

    public void start(String[] args) {
        Optional<Config> maybeConfig = parseConfig(args);
        if ( ! maybeConfig.isPresent() ) {
            logger.warn("Failed to parse configuration options.");
            printUsage();
            return;
        }

        final Config config = maybeConfig.orElse(null);
        SettingService settingService = new SettingService(config.getConfigFile());
        MailManager mailManager = new DefaultMailManager();
        DefaultMailSettingProvider mailSettingProvider = new DefaultMailSettingProvider(settingService);
        mailManager.setMailSettingProvider(mailSettingProvider);

    }

    private Optional<Config> parseConfig(String[] args) {
        logger.debug("Args: {} (3 expected)", args.length);
        if ( args != null && args.length == 3 ) {
            return Optional.ofNullable(new Config(args[0], args[1], args[2]));
        }
        return Optional.empty();
    }

    private void printUsage() {
        System.err.println();
        System.err.println("Usage: java -jar csv2mail.jar config-file-path html-template-path csv-path");
        System.err.println();
    }

}


