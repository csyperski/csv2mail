package com.cwssoft.csv2mail.mail;

import com.cwssoft.csv2mail.settings.SettingService;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DefaultQueueProcessor implements Runnable {

    public static final Logger logger = LoggerFactory.getLogger(DefaultQueueProcessor.class);

    private final MailManager mailManager;

    private final SettingService settingService;

    public DefaultQueueProcessor(MailManager mailManager, SettingService settingService) {
        this.mailManager = mailManager;
        this.settingService = settingService;
    }

    @Override
    public void run() {
        if (mailManager == null) {
            logger.warn("Invalid mail manager.");
            return;
        }

        if (settingService == null) {
            logger.warn("Invalid Setting Service.");
            return;
        }

        final int queueTimeout = settingService.getSetting(SettingService.EMAIL_QUEUE_TIMEOUT)
                .map(Ints::tryParse)
                .filter( v -> v != null )
                .orElse(60);

        final String filename = settingService.getSetting(SettingService.EMAIL_CSV_OUTPUT_FILENAME)
                .orElse("file.csv");

        final String description = settingService.getSetting(SettingService.EMAIL_CSV_OUTPUT_DESCRIPTION)
                .orElse("");



        Optional<MailQueue> maybeQueue = mailManager.getOptionalMailQueue();
        if (!maybeQueue.isPresent()) {
            logger.warn("Invalid mail queue.");
            return;
        }

        try {
            logger.debug("Starting queue processing thread (Timeout: {})...", queueTimeout);
            MailQueue q = maybeQueue.orElse(null);

            boolean keepGoing = true;
            MailQueueTask t = null;
            while (keepGoing) {
                logger.debug("Asking queue for another task...");
                t = q.getQueue().poll(queueTimeout, TimeUnit.SECONDS);
                if (t != null) {
                    logger.info("Sending: {}", t);
                    boolean success = false;

                    success = mailManager.sendMessage(t.getTitle(), t.getHtmlBody(), t.getTextBody(),
                            t.getTo(), t.getDataSources().stream().findFirst().orElse(null), filename, description);

                    if (success) {
                        logger.info("Message sent to: {}", t.getTo());
                    } else {
                        logger.warn("Failed to sent to: {}", t.getTo());
                    }
                } else {
                    keepGoing = false;
                }
            }
            logger.info("Exiting mail queue due to timeout.");
        } catch (InterruptedException e) {
            logger.warn("Interrupted: {}", e.getMessage(), e);
        }
    }
}
