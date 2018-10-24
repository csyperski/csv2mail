package com.cwssoft.csv2mail.mail;

import com.cwssoft.csv2mail.settings.MailSettingProvider;

import java.io.Serializable;
import java.util.Optional;
import javax.activation.DataSource;

/**
 *
 * @author csyperski
 */
public interface MailManager extends Serializable {

    boolean sendMessage(String title, String htmlBody, String textBody, String to);

    boolean sendMessage(String title, String htmlBody, String textBody, String to, DataSource ds, String name, String description);

    void setMailSettingProvider(MailSettingProvider mailSettingProvider);

    MailSettingProvider getMailSettingProvider();

    boolean isEnabled();

    Optional<MailQueue> getOptionalMailQueue();

    void setMailQueue(MailQueue q);

    boolean enqueue(MailQueueTask mailQueueTask);
}
