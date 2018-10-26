package com.cwssoft.csv2mail.settings;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailSettingTest {

    @Test
    @DisplayName("getValue - Mostly pointless")
    void getValue() {
        MailSetting instance = new MailSetting("key", "value");
        String result = instance.getValue().orElse(null);

        assertEquals("value", result);

    }

    @Test
    @DisplayName("getValueAsLong - 10")
    void getValueAsLong1() {
        MailSetting instance = new MailSetting("key", "10");
        long result = instance.getValueAsLong().orElse(-1l);
        assertEquals(10l, result);
    }

    @Test
    @DisplayName("getValueAsLong - Max")
    void getValueAsLong2() {
        MailSetting instance = new MailSetting("key", Long.MAX_VALUE + "");
        long result = instance.getValueAsLong().orElse(-1l);
        assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    @DisplayName("getValueAsLong - Min")
    void getValueAsLong3() {
        MailSetting instance = new MailSetting("key", Long.MIN_VALUE + "");
        long result = instance.getValueAsLong().orElse(-1l);
        assertEquals(Long.MIN_VALUE, result);
    }

    @Test
    @DisplayName("getValueAsLong - string")
    void getValueAsLong4() {
        MailSetting instance = new MailSetting("key", "test");
        long result = instance.getValueAsLong().orElse(-1l);
        assertEquals(-1l, result);
    }

    @Test
    @DisplayName("getValueAsBoolean - t")
    void getValueAsBoolean1() {
        MailSetting instance = new MailSetting("key", "t");
        boolean result = instance.getValueAsBoolean().orElse(Boolean.FALSE);
        assertEquals(true, result);
    }

    @Test
    @DisplayName("getValueAsBoolean - 1")
    void getValueAsBoolean2() {
        MailSetting instance = new MailSetting("key", "1");
        boolean result = instance.getValueAsBoolean().orElse(Boolean.FALSE);
        assertEquals(true, result);
    }
}