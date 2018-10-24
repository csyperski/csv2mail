package com.cwssoft.csv2mail;

import com.cwssoft.csv2mail.csv.CsvParser;
import com.cwssoft.csv2mail.mail.*;
import com.cwssoft.csv2mail.settings.DefaultMailSettingProvider;
import com.cwssoft.csv2mail.settings.SettingService;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.cwssoft.csv2mail.mail.EmailUtils.isValidEmail;

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
        if ( config != null ) {
            SettingService settingService = new SettingService(config.getConfigFile());

            MailManager mailManager = new DefaultMailManager();
            mailManager.setMailQueue(new PriorityMailQueue());
            mailManager.setMailSettingProvider(
                    new DefaultMailSettingProvider(settingService)
            );

            final String emailColumn = settingService.getSetting(SettingService.EMAIL_COLUMN).orElse(null);
            final String title = settingService.getSetting(SettingService.EMAIL_SUBJECT).orElse("** No Subject **");
            final String textBody = settingService.getSetting(SettingService.EMAIL_TEXT_BODY).orElse("");

            CsvParser csvParser = new CsvParser(config.getCsvFile(), emailColumn );
            Map<String, List<CSVRecord>> recordMap = csvParser.parse();

            final MailQueue queue = mailManager.getOptionalMailQueue().orElse(null);
            Thread queueProcessor = new Thread(
                    () -> {
                        if ( queue != null ) {
                            try {
                                boolean keepGoing = true;
                                MailQueueTask t = null;
                                while( keepGoing  ) {
                                    t = queue.getQueue().poll(1, TimeUnit.MINUTES);
                                    if ( t != null ) {
                                        logger.info("Sending: {}", t);
                                        boolean success = mailManager.sendMessage(t.getTitle(), t.getHtmlBody(), t.getTextBody(), t.getTo(), t.getDataSources().stream().findFirst().orElse(null), "file.csv", "");
                                        if ( success ) {
                                            logger.info("Message sent to: {}", t.getTo());
                                        } else {
                                            logger.warn("Failed to sent to: {}", t.getTo());
                                        }
                                    } else {
                                        keepGoing = false;
                                    }
                                }
                                logger.info("Exiting mail queue due to timeout.");
                            } catch( InterruptedException e) {
                                logger.warn("Interrupted: {}", e.getMessage(), e);
                            }
                        }
                    }
            );
            queueProcessor.start();

            // load the template
            Optional<BodyTemplate> maybeBodyTemplate = TemplateLoader.load(config.getHtmlEmailBodyFile());
            if ( maybeBodyTemplate.isPresent() ) {
                final BodyTemplate bodyTemplate = maybeBodyTemplate.orElse(null);

                final Set<String> allEmailAddresses = recordMap.keySet();
                for( String email : allEmailAddresses ) {
                    if ( email != null && isValidEmail(email) ) {
                        String body = bodyTemplate.process(email, recordMap.get(email), Collections.emptyMap());
                        if ( body != null ) {
                            MailQueueTask queueTask = new MailQueueTask(email, title, body, textBody);
                            mailManager.enqueue(queueTask);
                        }
                    } else {
                        logger.warn("Invalid email address, skipping: {}", email);
                    }
                }
            }
        }
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


