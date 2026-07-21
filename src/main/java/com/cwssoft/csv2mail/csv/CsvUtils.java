package com.cwssoft.csv2mail.csv;

import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.stream.Collectors;

public class CsvUtils {

    public static List<String[]> convertCsvRecordsToList(List<CSVRecord> records) {
        return records.stream()
                .map(record -> {
                    String[] row = new String[record.size()];
                    for (int i = 0; i < record.size(); i++) {
                        row[i] = record.get(i);
                    }
                    return row;
                })
                .collect(Collectors.toList());
    }

}
