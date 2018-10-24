package com.cwssoft.csv2mail.settings;

import java.util.Optional;

public class DefaultMailSettingProvider implements MailSettingProvider {

    private static final String ENABLED =  "mail.enabled";
    private static final String HOST =  "mail.host";
    private static final String USER =  "mail.user";
    private static final String PASS =  "mail.pass";
    private static final String PORT =  "mail.port";
    private static final String FROM =  "mail.from";
    private static final String SSL =  "mail.sslonconnect";
    private static final String DEBUG =  "mail.debug";
    private static final String RATELIMIT =  "mail.rate.limit";

    private final SettingService settingService;

    public DefaultMailSettingProvider(SettingService settingService) {
        this.settingService = settingService;
    }

    @Override
    public Optional<MailSetting> getByKey(String key) {
        if ( key != null ) {
            return settingService.getSetting(key).map( s -> new MailSetting(key, s));
        }
        return Optional.empty();
    }

    @Override
    public Optional<MailSetting> getHost() {
        return getByKey(HOST);
    }

    @Override
    public Optional<MailSetting> getEnabled() {
        return getByKey(ENABLED);
    }

    @Override
    public Optional<MailSetting> getFrom() {
        return getByKey(FROM);
    }

    @Override
    public Optional<MailSetting> getUser() {
        return getByKey(USER);
    }

    @Override
    public Optional<MailSetting> getPassword() {
        return getByKey(PASS);
    }

    @Override
    public Optional<MailSetting> getPort() {
        return getByKey(PORT);
    }

    @Override
    public Optional<MailSetting> getSslOnConnect() {
        return getByKey(SSL);
    }

    @Override
    public Optional<MailSetting> getDebug() {
        return getByKey(DEBUG);
    }

    @Override
    public Optional<MailSetting> getRateLimit() {
        return getByKey(RATELIMIT);
    }
}
