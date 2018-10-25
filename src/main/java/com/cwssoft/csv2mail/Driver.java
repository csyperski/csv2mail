package com.cwssoft.csv2mail;

import com.cwssoft.csv2mail.csv.CsvBuilder;
import com.cwssoft.csv2mail.csv.CsvParser;
import com.cwssoft.csv2mail.mail.*;
import com.cwssoft.csv2mail.settings.DefaultMailSettingProvider;
import com.cwssoft.csv2mail.settings.SettingService;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static com.cwssoft.csv2mail.mail.EmailUtils.isValidEmail;
import static com.cwssoft.csv2mail.settings.SettingService.EMAIL_QUEUE_SIZE;
import static com.cwssoft.csv2mail.settings.SettingService.EMAIL_QUEUE_TIMEOUT;

public class Driver {

    private static final String CSV_MIME = "text/csv";

    public static final Logger logger = LoggerFactory.getLogger(Driver.class);

    public static void main(String[] args) {
        new Driver(args);
    }

    public Driver(String[] args) {
        logger.debug("Starting csv2email...");
        start(args);
    }

    public void start(String[] args) {
        // command line arguments
        Optional<Config> maybeConfig = parseConfig(args);
        if (!maybeConfig.isPresent()) {
            logger.warn("Failed to parse configuration options.");
            printUsage();
            System.exit(1);
            return;
        }

        final Config config = maybeConfig.orElse(null);

        // setting service to pull settings from our properties file
        final SettingService settingService = new SettingService(config.getConfigFile());

        if ( ! settingService.load() ) {
            logger.warn("Failed to load settings from: {}", config.getConfigFile());
            System.exit(1);
            return;
        }

        // load a few of the required settings
        final String emailColumn = settingService.getSetting(SettingService.EMAIL_COLUMN).orElse(null);

        final int queueSize = settingService.getSetting(EMAIL_QUEUE_SIZE).map(s -> Ints.tryParse(s)).filter(s -> s != null).orElse(10000);

        // setup the mail system
        final MailManager mailManager = new DefaultMailManager();
        mailManager.setMailQueue(new PriorityMailQueue(queueSize));
        mailManager.setMailSettingProvider(
                new DefaultMailSettingProvider(settingService)
        );

        // load the template
        final Optional<BodyTemplate> maybeBodyTemplate = TemplateLoader.load(config.getHtmlEmailBodyFile());
        if (!maybeBodyTemplate.isPresent()) {
            logger.warn("Failed to load email template from: {}", config.getHtmlEmailBodyFile());
            System.exit(1);
            return;
        }

        // eagerly load the CSV and group by email
        final CsvParser csvParser = new CsvParser(config.getCsvFile(), emailColumn);
        final Map<String, List<CSVRecord>> recordMap = csvParser.parse();

        // this thread processes all the mail items and does the actual sending
        final Thread queueProcessor = new Thread(new DefaultQueueProcessor(mailManager, settingService));

        final BodyTemplate bodyTemplate = maybeBodyTemplate.orElse(null);
        final Set<String> allEmailAddresses = recordMap.keySet();
        logger.debug("Found {} email address to send to.", allEmailAddresses.size());
        if (!allEmailAddresses.isEmpty()) {
            queueProcessor.start();
            int count = allEmailAddresses.stream().mapToInt( email -> enqueueSingle(mailManager, settingService, email, recordMap.get(email), bodyTemplate ) ? 1 : 0).sum();
            logger.info("Queued {} email messages for delivery.", count);
        }
    }

    private boolean enqueueSingle(final MailManager mailManager,
                                  final SettingService settingService,
                                  final String email,
                                  final List<CSVRecord> records,
                                  final BodyTemplate bodyTemplate ) {

        if ( settingService == null ) {
            logger.warn("Skipping: {} - Invalid setting service.", email);
            return false;
        }

        final String title = settingService.getSetting(SettingService.EMAIL_SUBJECT).orElse("** No Subject **");
        final String textBody = settingService.getSetting(SettingService.EMAIL_TEXT_BODY).orElse("");
        final String csvOutputFields = settingService.getSetting(SettingService.EMAIL_CSV_OUTPUT).orElse("");

        if ( email == null || ! isValidEmail(email)) {
            logger.warn("Skipping: {} - Invalid email address.", email);
            return false;
        }

        if ( records == null || records.isEmpty()) {
            logger.warn("Skipping: {} - No records found.");
            return false;
        }

        if ( title == null || title.trim().length() == 0 ) {
            logger.warn("Skipping: {} - Invalid message title.");
            return false;
        }

        if ( textBody == null || textBody.trim().length() == 0 ) {
            logger.warn("Skipping: {} - Invalid textBody.");
            return false;
        }

        if ( csvOutputFields == null || csvOutputFields.trim().length() == 0 ) {
            logger.warn("Skipping: {} - Invalid csvOutputFields.");
            return false;
        }

        if ( bodyTemplate == null  ) {
            logger.warn("Skipping: {} - Invalid Body Template.");
            return false;
        }


        final List<String> csvFields = Splitter.on(CharMatcher.anyOf(","))
                .omitEmptyStrings()
                .trimResults().splitToList(csvOutputFields);

        Optional<String> maybeSingleEmailData = buildSingleEmailCsv(csvFields, records);

        String body = bodyTemplate.process(email, records);
        if (body != null) {

            Optional<DataSource> ds = Optional.empty();
            if ( maybeSingleEmailData.isPresent() ) {
                try (InputStream inputStream = new ByteArrayInputStream(maybeSingleEmailData.map(s -> s.getBytes()).orElse( new byte[] {} ));) {
                    ds = Optional.of(new ByteArrayDataSource(inputStream, CSV_MIME));
                } catch (Exception e) {
                    logger.warn("Failed to create attachment: {}", e.getMessage(), e);
                }
            }

            MailQueueTask queueTask = new MailQueueTask(email, title, body, textBody);
            ds.ifPresent( queueTask.getDataSources()::add );

            mailManager.enqueue(queueTask);
            return true;
        } else {
            logger.warn("Skipping: {} - Invalid Body.");
        }

        return false;
    }

    private Optional<String> buildSingleEmailCsv(final List<String> csvFields, final List<CSVRecord> records) {
        if ( csvFields == null || csvFields.isEmpty() ) {
            logger.warn("Unable to build CSV file: field list was empty or null");
            return Optional.empty();
        }

        if ( records == null || records.isEmpty() ) {
            logger.warn("Unable to build CSV file: records list was empty or null");
            return Optional.empty();
        }

        final CsvBuilder csv = new CsvBuilder();
        if ( ! csvFields.isEmpty() ) {
            csvFields.stream().forEachOrdered(f -> csv.addCell(f));
            csv.newLine();
        }

        records.stream().forEachOrdered( row -> {
            csvFields.stream().forEachOrdered( field -> {
                csv.addCell(row.get(field));
            });
            csv.newLine();
        });
        return Optional.ofNullable(csv.toString());
    }

    private Optional<Config> parseConfig(String[] args) {
        logger.debug("Args: {} (3 expected)", args.length);
        if (args != null && args.length == 3) {
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


