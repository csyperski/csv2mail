package com.cwssoft.csv2mail.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static com.cwssoft.csv2mail.mail.EmailUtils.isValidEmail;

public class CsvParser {

    private final static Logger logger = LoggerFactory.getLogger(CsvParser.class);

    private final String csvFile;

    private final String emailColumn;

    public CsvParser(String csvFile, String emailColumn) {
        this.csvFile = csvFile;
        this.emailColumn = emailColumn;
    }

    public Map<String, List<CSVRecord>> parse() {
        try (Reader in = new FileReader(csvFile)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);

            final Map<String, List<CSVRecord>> recordMap = new HashMap<>();

            for (CSVRecord record : records) {
                final String email = record.get(emailColumn);

                if (email != null && email.trim().length() > 0) {
                    final String cleanEmail = email.trim().toLowerCase(Locale.US);
                    if (isValidEmail(cleanEmail)) {
                        if (recordMap.containsKey(cleanEmail)) {
                            recordMap.get(cleanEmail).add(record);
                        } else {
                            final List<CSVRecord> list = new ArrayList<>();
                            list.add(record);
                            recordMap.putIfAbsent(cleanEmail, list);
                        }
                    } else {
                        logger.warn("Rejecting row: Invalid email. {} ", record.toString());
                    }
                } else {
                    logger.warn("Rejecting row: null or blank email. {}", record.toString());
                }

            }

            return recordMap;

        } catch (IOException ioEx) {
            logger.warn("Failed to load csv file: {} - Due to: {}", csvFile, ioEx.getMessage(), ioEx);
        }
        return Collections.emptyMap();
    }
}
