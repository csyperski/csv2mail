package com.cwssoft.csv2mail.settings;

import java.io.Serializable;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author csyperski
 */
public final class MailSetting implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(MailSetting.class);
    
    public static final String KEY_ENABLED = "com.cwssoft.smtp.enabled";

    public static final String KEY_HOST = "com.cwssoft.smtp.host";

    public static final String KEY_FROM = "com.cwssoft.smtp.from";

    public static final String KEY_USER = "com.cwssoft.smtp.user";

    public static final String KEY_PASSWORD = "com.cwssoft.smtp.password";

    public static final String KEY_PORT = "com.cwssoft.smtp.port";

    public static final String KEY_TLS = "com.cwssoft.smtp.tls";

    public static final String KEY_DEBUG = "com.cwssoft.smtp.debug";

    public static final String KEY_RATELIMIT = "com.cwssoft.smtp.ratelimit";

    private final String settingValue;

    private final String key;

    public MailSetting(String key, String value) {
        this.settingValue = value;
        this.key = key;
    }

    public Optional<Long> getValueAsLong() throws NumberFormatException {
        try {
            return Optional.of(Long.parseLong(settingValue));
        } catch( NumberFormatException nfe ) {
            logger.warn("Unable to parse long: {} {}", settingValue, nfe.getMessage(), nfe);
            return Optional.empty();
        }
    }

    public Optional<Integer> getValueAsInt() throws NumberFormatException {
        try {
            return Optional.of(Integer.parseInt(settingValue));
        } catch( NumberFormatException nfe ) {
            logger.warn("Unable to parse int: {} {}", settingValue, nfe.getMessage(), nfe);
            return Optional.empty();
        }
    }

    public Optional<Boolean> getValueAsBoolean() throws IllegalArgumentException {
        if (settingValue != null) {
            switch (settingValue.toLowerCase().trim()) {
                case "1":
                case "true":
                case "t":
                case "yes":
                case "y":
                    return Optional.of(Boolean.TRUE);
                case "0":
                case "false":
                case "f":
                case "no":
                case "n":
                    return Optional.of(Boolean.FALSE);
                default:
                    return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * @return the value
     */
    public Optional<String> getValue() {
        return Optional.ofNullable(settingValue);
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

}
