package com.cwssoft.csv2mail.csv;

public class CsvParser {

    private final String csvFile;

    private final String emailColumn;

    public CsvParser(String csvFile, String emailColumn) {
        this.csvFile = csvFile;
        this.emailColumn = emailColumn;
    }
}
