package com.cwssoft.csv2mail.mail;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BodyTemplate {

    private final static Logger logger = LoggerFactory.getLogger(BodyTemplate.class);

    private static final String XRECIPIENTEMAIL = "${x-recipient-email}";
    private static final String XCURRENTDATE = "${x-current-date}";
    private static final String XTEMPLATESTART = "${x-repeat-start}";
    private static final String XTEMPLATEEND = "${x-repeat-end}";

    private String body;

    public BodyTemplate(String body) {
        this.body = body;
    }

    public String process(String email, List<CSVRecord> records, Map<String, String> otherValues) {
        if ( body != null && records != null ) {

            String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date());

            String result = body.replace(XCURRENTDATE, currentDate);
            result = result.replace(XRECIPIENTEMAIL, email);

            // lets get the prefix/template/postfix
            final String prefix = getPrefix(result);
            final String postfix = getPostfix(result);
            final String template = getTemplate(result);

            final List<String> middleLines = new ArrayList<>();

            for( CSVRecord record : records ) {
                if ( record != null ) {
                    String row = processTemplate(template, record);
                    if ( row != null ) {
                        middleLines.add(row);
                    }
                }
            }

            return prefix + middleLines.stream().collect(Collectors.joining("")) + postfix;
        }
        return body;
    }

    protected String processTemplate(String template, CSVRecord record) {
        if( template != null && record != null ) {
            String result = template;
            for ( String key : record.toMap().keySet() ) {
                if ( key != null ) {

                    List<String> keys = new ArrayList<>();
                    String cleanKey = key.trim();

                    keys.add(cleanKey);
                    keys.add(cleanKey.replace('_', '-').replace(' ', '-'));

                    for( String k : keys ) {
                        result = result.replace("${" + k + "}", record.get(key));
                    }
                }
            }
            return result;
        }
        return template;
    }

    protected String getPrefix(String body) {

        String regexString = "^(.*?)" + Pattern.quote(XTEMPLATESTART) + ".*$";
        Pattern pattern = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE  | Pattern.DOTALL  );

        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {

            return matcher.group(1);
        }
        return null;
    }

    protected String getTemplate(String body) {
        // we are going to extract the template
        String regexString = "^.*" + Pattern.quote(XTEMPLATESTART) + "(.*)" + Pattern.quote(XTEMPLATEEND) + ".*$";
        Pattern pattern = Pattern.compile(regexString,  Pattern.DOTALL  );
        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    protected String getPostfix(String body) {

        String regexString = "^.*" + Pattern.quote(XTEMPLATEEND) + "(.*?)$";
        Pattern pattern = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL  );

        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
