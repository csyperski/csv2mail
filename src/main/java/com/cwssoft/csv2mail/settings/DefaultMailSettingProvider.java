package com.cwssoft.csv2mail.settings;

import com.cwssoft.mail.settings.MailSetting;
import com.cwssoft.mail.settings.MailSettingProvider;

import java.util.Optional;

public class DefaultMailSettingProvider implements MailSettingProvider {

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
    public MailSetting getHost() {
        return getByKey(HOST).orElse(null);
    }

    @Override
    public MailSetting getEnabled() {
        return new MailSetting("mail.enabled", "true");
    }

    @Override
    public MailSetting getFrom() {
        return getByKey(FROM).orElse(null);
    }

    @Override
    public MailSetting getUser() {
        return getByKey(USER).orElse(null);
    }

    @Override
    public MailSetting getPassword() {
        return getByKey(PASS).orElse(null);
    }

    @Override
    public MailSetting getPort() {
        return getByKey(PORT).orElse(null);
    }

    @Override
    public MailSetting getTls() {
        return getByKey(SSL).orElse(null);
    }

    @Override
    public MailSetting getDebug() {
        return getByKey(DEBUG).orElse(null);
    }

    @Override
    public MailSetting getRateLimit() {
        return getByKey(RATELIMIT).orElse(null);
    }
}
