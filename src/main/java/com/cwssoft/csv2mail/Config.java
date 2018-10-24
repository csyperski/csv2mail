package com.cwssoft.csv2mail;

public class Config {

    private final String configFile;
    private final String htmlEmailBodyFile;
    private final String csvFile;

    public Config(String configFile, String htmlEmailBodyFile, String csvFile) {
        this.configFile = configFile;
        this.htmlEmailBodyFile = htmlEmailBodyFile;
        this.csvFile = csvFile;
    }

    public String getHtmlEmailBodyFile() {
        return htmlEmailBodyFile;
    }

    public String getConfigFile() {
        return configFile;
    }

    public String getCsvFile() {
        return csvFile;
    }
}
