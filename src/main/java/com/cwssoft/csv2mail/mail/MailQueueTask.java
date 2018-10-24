package com.cwssoft.csv2mail.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.activation.DataSource;

/**
 *
 * @author csyperski
 */
public class MailQueueTask implements Serializable {

    public static final int PRIORITY_HIGH = 1;

    public static final int PRIORITY_NORMAL = 2;

    public static final int PRIORITY_LOW = 3;

    private final String title;

    private final String htmlBody;

    private final String textBody;

    private final String to;

    private final int priority;

    // attachment things
    private final List<DataSource> dataSources = new ArrayList<>();

    public MailQueueTask(String to, String title, String html, String text) {
        this(to, title, html, text, PRIORITY_NORMAL);
    }

    public MailQueueTask( String to, String title, String html, String text, int priority) {
        this.title = title;
        this.htmlBody = html;
        this.textBody = text;
        this.to = to;
        this.priority = priority;
    }

    /**
     * @return the to
     */
    public String getTo() {
        return to;
    }


    @Override
    public String toString() {
        return "MailQueueTask{to=" + to + ", priority=" + priority + '}';
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public String getTextBody() {
        return textBody;
    }

    public List<DataSource> getDataSources() {
        return dataSources;
    }
}
