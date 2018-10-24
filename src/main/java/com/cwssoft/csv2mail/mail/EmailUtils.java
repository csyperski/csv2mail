package com.cwssoft.csv2mail.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EmailUtils {

    private static final Logger logger = LoggerFactory.getLogger(EmailUtils.class);

    public static boolean isValidEmail(String email) {
        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
            return true;
        } catch (AddressException ex) {
            logger.warn("Failed to validate email: {} due to: {}", email, ex.getMessage(), ex);
        }
        return false;
    }

}
