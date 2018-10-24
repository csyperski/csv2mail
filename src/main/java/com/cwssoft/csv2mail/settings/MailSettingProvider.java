/*
 *  Copyright (C) 2016 Charles Syperski <csyperski@gmail.com> - CWS Software LLC
 */
package com.cwssoft.csv2mail.settings;

import java.util.Optional;

/**
 *
 * @author csyperski
 */
public interface MailSettingProvider {
    public Optional<MailSetting> getByKey(String key);
    public Optional<MailSetting> getHost();
    public Optional<MailSetting> getEnabled();
    public Optional<MailSetting> getFrom();
    public Optional<MailSetting> getUser();
    public Optional<MailSetting> getPassword();
    public Optional<MailSetting> getPort();
    public Optional<MailSetting> getSslOnConnect();
    public Optional<MailSetting> getDebug();
    public Optional<MailSetting> getRateLimit();
}
