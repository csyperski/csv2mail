package com.cwssoft.csv2mail.mail;

import static com.cwssoft.csv2mail.mail.EmailUtils.isValidEmail;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.cwssoft.csv2mail.settings.MailSetting;
import com.cwssoft.csv2mail.settings.MailSettingProvider;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.activation.DataSource;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author csyperski
 */
public class DefaultMailManager implements MailManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMailManager.class);

    private LoadingCache<String, Optional<MailSetting>> mailSettings;

    private MailSettingProvider mailSettingProvider;

    private volatile boolean initCalled = false;

    private RateLimiter rateLimiter;

    private MailQueue mailQueue;

    public void initCaches() {
        if (initCalled) {
            return;
        }

        synchronized (this) {
            if (initCalled) {
                return;
            }

            logger.debug("Loading email Settings...");

            rateLimiter = RateLimiter.create(mailSettingProvider.getRateLimit()
                    .filter( s -> s.getValue().map( z -> z.trim().length() > 0 ).orElse(Boolean.FALSE))
                    .flatMap(s -> s.getValueAsInt()).orElse(3)
            );

            logger.info("  + Rate Limiter rate: {}", rateLimiter.getRate());
            mailSettings = CacheBuilder.newBuilder().maximumSize(20).expireAfterWrite(2, TimeUnit.MINUTES).build(new CacheLoader<String, Optional<MailSetting>>() {
                @Override
                public Optional<MailSetting> load(String key) {
                    logger.debug("Going to provider for setting {}", key);
                    return mailSettingProvider.getByKey(key);
                }
            });

            initCalled = true;
        }
    }

    private Optional<MailSetting> getSetting(String key) {
        if (key != null) {
            initCaches();
            try {
                return mailSettings.get(key);
            } catch (ExecutionException ex) {
                logger.warn("Error getting setting: {} - {}", key, ex.getMessage(), ex);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isEnabled() {
        return mailSettingProvider.getEnabled().flatMap( MailSetting::getValueAsBoolean ).orElse(Boolean.FALSE);
    }

    private Optional<String> getSmtpHost() {
        return mailSettingProvider.getHost().flatMap( MailSetting::getValue );
    }

    private Optional<String> getSmtpFrom() {
        return mailSettingProvider.getFrom().flatMap( MailSetting::getValue );
    }

    private boolean isSmtpTls() {
        return mailSettingProvider.getSslOnConnect().flatMap( s -> s.getValueAsBoolean() ).orElse(Boolean.FALSE);
    }

    private int getSmtpPort() {
        return mailSettingProvider.getPort()
                .filter( s -> s.getValue().map( z -> z.trim().length() > 0).orElse(Boolean.FALSE) )
                .flatMap( MailSetting::getValueAsInt ).orElse( 25 );
    }

    private Optional<String> getSmtpUser() {
        return mailSettingProvider.getUser().flatMap( MailSetting::getValue );
    }

    private Optional<String> getSmtpPassword() {
        return mailSettingProvider.getPassword().flatMap( MailSetting::getValue );
    }

    private boolean isDebugging() {
        return mailSettingProvider.getDebug().flatMap( s -> s.getValueAsBoolean() ).orElse(Boolean.FALSE);
    }


    @Override
    public boolean sendMessage(String title, String htmlBody, String textBody, String to) {
        return sendMessage(title, htmlBody, textBody, to, null, null, null);
    }

    @Override
    public boolean sendMessage(String title, String htmlBody, String textBody, String to, DataSource ds, String name, String description) {

        if (!isEnabled()) {
            logger.warn("Unable to send message, system is disabled.");
            return false;
        }

        initCaches();

        String host = getSmtpHost().orElse(null);
        String from = getSmtpFrom().orElse(null);
        int port = getSmtpPort();

        if (isNullOrEmpty(host)) {
            logger.warn("SMTP server value is blank.");
            return false;
        }

        if (isNullOrEmpty(title)) {
            logger.warn("Message title is blank.");
            return false;
        }

        if (isNullOrEmpty(from) || !isValidEmail(from)) {
            logger.warn("SMTP from address value is blank or invalid.");
            return false;
        }

        if (isNullOrEmpty(to) || !isValidEmail(to)) {
            logger.warn("To: address value is blank or invalid.");
            return false;
        }

        if (isNullOrEmpty(htmlBody) && isNullOrEmpty(textBody)) {
            logger.warn("Either a HTML body or Text body is required");
            return false;
        }

        try {

            HtmlEmail email = new HtmlEmail();
            email.setHostName(host);
            email.setSmtpPort(port);
            email.setDebug(isDebugging());

            if (isSmtpTls()) {
                email.setSSLOnConnect(true);
            }

            String user = getSmtpUser().orElse(null);
            String pass = getSmtpPassword().orElse(null);
            if (!isNullOrEmpty(user)) {
                email.setAuthentication(user, pass);
            }

            email.addTo(to);
            email.setFrom(from);
            email.setSubject(title);
            email.setHtmlMsg(isNullOrEmpty(htmlBody) ? "Please switch to plain text mode to view this message." : htmlBody);
            email.setTextMsg(isNullOrEmpty(textBody) ? "Please switch to HTML or rich text mode to view this message." : textBody);

            if (ds != null) {
                email.attach(ds, name, description, EmailAttachment.ATTACHMENT);
            }
            double waitTime = rateLimiter.acquire();
            email.send();
            logger.info("Message sent to {} (delay: {} seconds).", to, waitTime);
            return true;
        } catch (EmailException ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    /**
     * @return the mailSettingProvider
     */
    @Override
    public MailSettingProvider getMailSettingProvider() {
        return mailSettingProvider;
    }

    /**
     * @param mailSettingProvider the mailSettingProvider to set
     */
    @Override
    public void setMailSettingProvider(MailSettingProvider mailSettingProvider) {
        this.mailSettingProvider = mailSettingProvider;
    }

    public Optional<MailQueue> getOptionalMailQueue() {
        return Optional.ofNullable(mailQueue);
    }

    @Override
    public boolean enqueue(MailQueueTask mailQueueTask) {
        if ( mailQueueTask == null ) {
            throw new NullPointerException("Invalid Mail Queue Task");
        }

        if ( mailQueue == null ) {
            throw new NullPointerException("Invalid Mail Queue");
        }

        final String to = mailQueueTask.getTo();
        if (isNullOrEmpty(to) || !isValidEmail(to)) {
            logger.warn("To: address value is blank or invalid.");
            return false;
        }

        final String htmlBody = mailQueueTask.getHtmlBody();
        final String textBody = mailQueueTask.getTextBody();
        if (isNullOrEmpty(htmlBody) && isNullOrEmpty(textBody)) {
            logger.warn("Either a HTML body or Text body is required");
            return false;
        }

        mailQueue.addTask(mailQueueTask);
        return true;
    }

    /**
     * @return the mailQueue
     */
    public MailQueue getMailQueue() {
        return mailQueue;
    }

    /**
     * @param mailQueue the mailQueue to set
     */
    @Override
    public void setMailQueue(MailQueue mailQueue) {
        this.mailQueue = mailQueue;
    }

}
